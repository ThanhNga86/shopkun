var instruction = document.querySelector(".instruction")
instruction.innerHTML = instruction.innerText

handleContainer() // default
function handleContainer() {
	// thay đổi container
	if (window.innerWidth <= 992) {
		var containerOrderH = document.querySelector("#containerOrderH")
		containerOrderH.setAttribute("class", "container-fluid px-0")
	} else {
		var containerOrderH = document.querySelector("#containerOrderH")
		containerOrderH.setAttribute("class", "container px-0")
	}

}

window.addEventListener("resize", handleContainer)

// tính tổng giá từng đơn hàng
totalPay()
function totalPay() {
	var order = document.querySelectorAll(".order")
	order.forEach((event) => {
		var total = 0;
		var totalPay = event.querySelector(".totalPay");
		var totals = event.querySelectorAll(".total")
		for (let i = 0; i < totals.length; i++) {
			total += Number(totals[i].innerText.replaceAll(".", "").replaceAll("đ", ""))
		}
		totalPay.querySelector('span').innerHTML = `${total.toLocaleString('Vi-VN')}đ`
	})
}

// hủy đơn hàng
cancelOrder()
function cancelOrder() {
	var cancel = document.querySelectorAll(".cancel")
	cancel.forEach((event) => {
		event.addEventListener("click", (e) => {
			var order = e.target.parentNode.parentNode.parentNode
			var orderId = e.target.id
			var waitForConfirmation = document.querySelector("#waitForConfirmation")

			swal({
				text: `Bạn chắc muốn hủy đơn hàng mã ${orderId}?`,
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/cancel-order",
							type: "post",
							data: {
								orderId: orderId
							},
							success: function(response) {
								order.remove()
								var lengthOr = document.querySelectorAll(".order").length
								if (lengthOr == 0) {
									waitForConfirmation.innerHTML = `<div class="not-order">Bạn chưa có đơn hàng nào !</div>`
								}

								var totalWait = document.querySelector(".totalWait")
								if (totalWait) {
									if (response != 0) {
										totalWait.innerHTML = response
										totalWait.style.display = "block"
									} else {
										totalWait.style.display = "none"
									}
								}

								filterByStatus()
							}
						})
					}
				})
		})
	})
}

// mua lại đơn hàng từ tab đã giao và đã hủy
function repurchaseOrder() {
	var repurchase = document.querySelectorAll(".repurchase")
	repurchase.forEach((event) => {
		event.addEventListener("click", (e) => {
			var orderId = e.target.id

			$.ajax({
				url: "/repurchase-order",
				type: "get",
				data: {
					orderId: orderId,
				},
				success: function() {
					location.href = "/shopping-cart"
				}
			})
		})
	})
}

// tìm kiếm đơn hàng
var search = document.querySelector(".search-order")

search.addEventListener("input", (e) => {
	var row = e.target.parentNode.parentNode
	var status = row.querySelector(".nav-link.active").querySelector(".btnNav").innerHTML
	var tabPane = row.querySelector(".tab-pane.fade.show")
	var orderStatus = tabPane.querySelector(".orderStatus")
	var viewMore = tabPane.querySelector(".viewMore")

	orderStatus.innerHTML = `
		<div class="loadTab text-center">
			<div class="lds-dual-ring"></div>
		</div>
	`

	$.ajax({
		url: "/search-order",
		type: "get",
		data: {
			query: e.target.value,
			status: status
		},
		success: function(response) {
			var body = response.split('|')
			orderStatus.innerHTML = body[0]

			if (body[1]) {
				if (viewMore) {
					viewMore.innerHTML = body[1]
				}
			} else {
				if (viewMore) {
					viewMore.innerHTML = ''
				}
			}
			totalPay()
			cancelOrder()
			repurchaseOrder()
			pagingOrder()
		}
	})
})

// load dữ liệu đơn hàng theo status
filterByStatus()
function filterByStatus() {
	var buttons = document.querySelectorAll(".nav-link")
	buttons.forEach((event, i) => {
		event.addEventListener("click", () => {
			var btn = buttons[i].querySelector(".btnNav")
			var id = buttons[i].getAttribute('data-bs-target').replace("#", "")
			findAllByStatus(btn.textContent, id)
		})
	})
}

function findAllByStatus(status, id) {
	var tabPane = document.querySelector(`#${id}`)
	var orderStatus = tabPane.querySelector(`.orderStatus`)
	var viewMore = tabPane.querySelector(".viewMore")

	orderStatus.innerHTML = `
		<div class="loadTab text-center">
			<div class="lds-dual-ring"></div>
		</div>
	`

	$.ajax({
		url: "/order-status",
		type: "get",
		data: {
			status: status
		},
		success: function(response) {
			body = response.split('|')

			orderStatus.innerHTML = body[0]

			if (body[1].includes('qtityP')) {
				var qtityP = body[1].replace("qtityP", "").split(":")
				var totalWait = document.querySelector(".totalWait")
				var totalConfirmed = document.querySelector(".totalConfirmed")
				var totalDelivering = document.querySelector(".totalDelivering")

				if (totalWait) {
					if (qtityP[0] != 0) {
						totalWait.innerHTML = qtityP[0]
						totalWait.style.display = "block"
					} else {
						totalWait.style.display = "none"
					}
				}
				if (totalConfirmed) {
					if (qtityP[1] != 0) {
						totalConfirmed.innerHTML = qtityP[1]
						totalConfirmed.style.display = "block"
					} else {
						totalConfirmed.style.display = "none"
					}
				}
				if (totalDelivering) {
					if (qtityP[2] != 0) {
						totalDelivering.innerHTML = qtityP[2]
						totalDelivering.style.display = "block"
					} else {
						totalDelivering.style.display = "none"
					}
				}
			} else {
				if (body[1]) {
					if (viewMore) {
						viewMore.innerHTML = body[1]
					}
				} else {
					if (viewMore) {
						viewMore.innerHTML = ''
					}
				}
			}

			totalPay()
			cancelOrder()
			repurchaseOrder()
			pagingOrder()
		}
	})
}

// paging order theo Đã mua và Đã hủy
function pagingOrder() {
	var pageNumberDelivered = 1
	var pageNumberCancelled = 1
	paging()

	function paging() {
		var delivered = document.querySelector(`#delivered`)
		var cancelled = document.querySelector(`#cancelled`)
		var btnViewMore = document.querySelectorAll(".btnViewMore")

		btnViewMore.forEach((event) => {
			event.addEventListener("click", () => {
				event.innerHTML = "Đang tải..."
				var status = event.id

				if (status == 'Đã giao') {
					pageNumberDelivered++;
				} else if (status == 'Đã hủy') {
					pageNumberCancelled++
				}

				$.ajax({
					url: "/paging-order",
					type: "get",
					data: {
						pageNumberDelivered: pageNumberDelivered,
						pageNumberCancelled: pageNumberCancelled,
						status: status
					},
					success: function(response) {
						var body = response.split("|")

						event.innerHTML = `Xem thêm đơn hàng <i class="fa-solid fa-angle-down"></i>`
						if (status == 'Đã giao') {
							var orderStatus = delivered.querySelector(`.orderStatus`)
							orderStatus.innerHTML += body[0]

							var viewMore = delivered.querySelector(".viewMore")
							if (body[1]) {
								if (viewMore) {
									viewMore.innerHTML = body[1]
								}
							} else {
								if (viewMore) {
									viewMore.innerHTML = ''
								}
							}
						} else if (status == 'Đã hủy') {
							var orderStatus = cancelled.querySelector(`.orderStatus`)
							orderStatus.innerHTML += body[0]

							var viewMore = cancelled.querySelector(".viewMore")
							if (body[1]) {
								if (viewMore) {
									viewMore.innerHTML = body[1]
								}
							} else {
								if (viewMore) {
									viewMore.innerHTML = ''
								}
							}
						}

						totalPay()
						cancelOrder()
						repurchaseOrder()
						pagingOrder()
					}
				})
			})
		})
	}
}