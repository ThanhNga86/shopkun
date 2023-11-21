// cập nhật đơn hàng
function editOrder() {
	var saveData = ''
	var index = 0

	var edit = document.querySelectorAll(".edit")
	edit.forEach((event, i) => {
		var flagSave = true
		event.addEventListener("click", () => {
			index = i
			var data = edit[i].parentNode.parentNode
			saveData = data.querySelector(".data-save").innerHTML
			// gửi đi
			if (flagSave) {
				flagSave = updateOrder()
			}
		})
	})

	function updateOrder() {
		var btnSave = document.querySelectorAll(".btnSave")[index]
		btnSave.addEventListener("click", (e) => {
			if (validatedForm() == true) {
				btnSave.innerHTML = 'Đang lưu...'
				var formData = new FormData()
				var orderId = e.target.id
				var inpFullName = document.querySelectorAll(".inpFullName")[index]
				var inpAddress = document.querySelectorAll(".inpAddress")[index]
				var inpPhone = document.querySelectorAll(".inpPhone")[index]
				var inpNote = document.querySelectorAll(".inpNote")[index]
				var inpStatus = document.querySelectorAll(".inpStatus")[index]

				formData.set("id", orderId)
				formData.set("fullName", inpFullName.value)
				formData.set("address", inpAddress.value)
				formData.set("phone", inpPhone.value)
				formData.set("note", inpNote.value)
				formData.set("status", inpStatus.value)

				$.ajax({
					url: "/admin/update-order",
					type: "post",
					processData: false,  // Không xử lý dữ liệu
					contentType: false,  // Không cần ghi đè contentType
					data: formData,
					success: function(response) {
						if (response == 'success') {
							var orderId = document.querySelectorAll(".orderId")[index]
							sessionStorage.setItem("saveSC", orderId.innerText)
							location.reload()
						}
					}
				})
			}
		})

		return false;
	}

	function validatedForm() {
		var check = true
		var inpFullName = document.querySelectorAll(".inpFullName")[index]
		var inpAddress = document.querySelectorAll(".inpAddress")[index]
		var inpPhone = document.querySelectorAll(".inpPhone")[index]

		var msFullName = document.querySelectorAll(".msFullname")[index]
		if (inpFullName.value.length <= 0) {
			msFullName.innerText = `Vui lòng nhập họ và tên`
			check = false
		}
		inpFullName.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msFullName.innerText = ""
			}
		})

		var msAddress = document.querySelectorAll(".msAddress")[index]
		if (inpAddress.value.length <= 0) {
			msAddress.innerText = `Vui lòng nhập địa chỉ`
			check = false
		}
		inpAddress.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msAddress.innerText = ""
			}
		})

		var msPhone = document.querySelectorAll(".msPhone")[index]
		if (inpPhone.value.length <= 0) {
			msPhone.innerText = `Vui lòng nhập số điện thoại`
			check = false
		}
		inpPhone.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msPhone.innerText = ""
			}
		})

		return check;
	}

	// làm mới dữ liệu như ban đầu
	resetForm()
	function resetForm() {
		var modal = document.querySelectorAll(".modal")
		modal.forEach((event) => {
			event.addEventListener("click", (e) => {
				var data = document.querySelectorAll(".data-save")[index]
				if (e.target.classList.contains('btnReset')
					|| e.target.classList.contains('btnCloseP')
					|| e.target.classList.contains('modal')) {
					data.innerHTML = saveData
				}
			})
		})
	}
}

// duyệt đơn hàng
var vlAccept = sessionStorage.getItem("accept")
if (vlAccept) {
	swal("", "Duyệt đơn hàng thành công", "success")
	sessionStorage.removeItem("accept")
}

acceptOrder()
function acceptOrder() {
	var accept = document.querySelectorAll(".accept")
	accept.forEach((event, i) => {
		event.addEventListener("click", () => {
			var row = accept[i].parentNode.parentNode
			var orderId = row.querySelector(".orderId").innerHTML
			var ctnStatus = row.querySelector(".status")
			var status = ctnStatus.querySelector("span").innerText

			swal({
				text: `Bạn chắc muốn duyệt đơn hàng có mã ${orderId}?`,
				buttons: ["Hủy", 'Đồng ý'],
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/admin/accept-order",
							type: "get",
							data: {
								orderId: orderId,
								status: status
							},
							success: function(response) {
								if (response == 'success') {
									location.reload()
									sessionStorage.setItem("accept", "accept")
								}
							}
						})
					}
				})
		})
	})
}

// xem chi tiết đơn hàng
showAndHiddenOrderDetail()
function showAndHiddenOrderDetail() {
	var viewDetail = document.querySelectorAll(".see")
	for (let i = 0; i < viewDetail.length; i++) {
		viewDetail[i].addEventListener("click", (e) => {
			showHiddenOrderD("show", i)
		})
	}

	var closeX = document.querySelectorAll(".hidden-orderDetail")
	for (let i = 0; i < closeX.length; i++) {
		closeX[i].addEventListener("click", () => {
			showHiddenOrderD("hidden", i)
		})
	}

	var showDetail = document.querySelectorAll(".show-orderDetails")
	for (let i = 0; i < showDetail.length; i++) {
		showDetail[i].addEventListener("click", (e) => {
			if (e.target.classList.contains('row')) {
				showHiddenOrderD("hidden", i)
			}
		})
	}

	var showDetails = document.querySelectorAll(".show-orderDetail")
	for (let i = 0; i < showDetails.length; i++) {
		showDetails[i].addEventListener("click", () => {
			showHiddenOrderD("hidden", i)
		})
	}

	function showHiddenOrderD(action, i) {
		var showDetail = document.querySelectorAll(".show-orderDetail")
		var showDetails = document.querySelectorAll(".show-orderDetails")

		if (action == 'show') {
			showDetail[i].style.display = 'block'
			showDetails[i].style.display = 'block'
			document.body.style.overflow = 'hidden'
		} else {
			showDetail[i].style.display = 'none'
			showDetails[i].style.display = 'none'
			document.body.style.overflow = 'auto'
		}

		// tính tổng số tiền
		var totalPay = document.querySelectorAll(".totalPay")[i]
		var totalOD = showDetails[i].querySelectorAll(".totalOD")
		var sum = 0
		for (let i = 0; i < totalOD.length; i++) {
			var sumP = totalOD[i].innerHTML.replaceAll(".", "").replaceAll('đ', '')
			sum += Number(sumP)
		}
		totalPay.innerHTML = `Tổng tiền: ${sum.toLocaleString('vi-VN')}đ`
	}
}

// xóa đơn hàng
removeOrder()
function removeOrder() {
	var remove = document.querySelectorAll(".remove")
	for (let i = 0; i < remove.length; i++) {
		remove[i].addEventListener("click", () => {
			var orderId = remove[i].id
			var row = remove[i].parentNode.parentNode

			swal({
				text: `Bạn chắc muốn xóa đơn hàng có mã ${orderId}?`,
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/admin/delete-order",
							type: "get",
							data: {
								orderId: orderId
							},
							success: function(response) {
								if (response == 'success') {
									swal("", "Xóa đơn hàng thành công", "success")
									row.remove()
								}
								else if (response == 'error') {
									swal("", "Đơn hàng không thể xóa", "error")
								}
							}
						})
					}
				})
		})
	}
}

// tìm kiếm đơn hàng theo mã số, họ tên, địa chỉ, số điện thoại
var vlSearch = sessionStorage.getItem("vlSearch")
if (vlSearch) {
	var tableOrder = document.querySelector(".tableOrder")
	var searchO = document.querySelector(".searchO")
	var paging = document.querySelector("#paging")
	var dataFilter = document.querySelector(".btnFilter").getAttribute("data-filter")
	searchO.value = vlSearch

	tableOrder.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

	if (paging) {
		if (searchO.value == '') {
			paging.style.display = "flex"
		} else {
			paging.style.display = "none"
		}
	}

	$.ajax({
		url: "/admin/search-order",
		type: "get",
		data: {
			query: searchO.value,
			dataFilter: dataFilter
		},
		success: function(response) {
			sessionStorage.setItem("vlSearch", searchO.value)
			tableOrder.innerHTML = response
			removeOrder()
			acceptOrder()
			showAndHiddenOrderDetail()

			var saveSC = sessionStorage.getItem("saveSC") // Nếu có save order thì thông báo như dưới và ngược lại là mặc định
			if (saveSC) {
				var orderId = document.querySelectorAll(".orderId")
				var flagId = false
				orderId.forEach((orderId, i) => {
					if (orderId.innerText == saveSC) {
						swal("", `Cập nhật đơn hàng thành công`, "success")
						editOrder()
						document.querySelectorAll(".edit")[i].click()
						flagId = true
					}
				})

				if (!flagId) {
					swal("", `Cập nhật đơn hàng thành công`, "success")
				}
				sessionStorage.removeItem("saveSC")
			} else {
				editOrder()
			}
		}
	})
} else {
	var saveSC = sessionStorage.getItem("saveSC") // Nếu có save order thì thông báo như dưới và ngược lại là mặc định
	if (saveSC) {
		var orderId = document.querySelectorAll(".orderId")
		var flagId = false
		orderId.forEach((orderId, i) => {
			if (orderId.innerText == saveSC) {
				swal("", `Cập nhật đơn hàng thành công`, "success")
				editOrder()
				document.querySelectorAll(".edit")[i].click()
				flagId = true
			}
		})

		if (!flagId) {
			swal("", `Cập nhật đơn hàng thành công`, "success")
		}
		sessionStorage.removeItem("saveSC")
	} else {
		editOrder()
	}
}

searchAndFilterOrder()
function searchAndFilterOrder() {
	var tableOrder = document.querySelector(".tableOrder")
	var searchO = document.querySelector(".searchO")
	var paging = document.querySelector("#paging")
	var dataFilter = document.querySelector(".btnFilter").getAttribute("data-filter")

	searchO.addEventListener("input", () => {
		tableOrder.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

		if (paging) {
			if (searchO.value == '') {
				paging.style.display = "flex"
			} else {
				paging.style.display = "none"
			}
		}

		setTimeout(() => {
			$.ajax({
				url: "/admin/search-order",
				type: "get",
				data: {
					query: searchO.value,
					dataFilter: dataFilter
				},
				success: function(response) {
					sessionStorage.setItem("vlSearch", searchO.value)
					tableOrder.innerHTML = response

					editOrder()
					removeOrder()
					acceptOrder()
					showAndHiddenOrderDetail()
				}
			})
		}, 500)
	})
}