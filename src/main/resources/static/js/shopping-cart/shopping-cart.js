var instruction = document.querySelector(".instruction")
instruction.innerHTML = instruction.innerText

var inputs = document.querySelectorAll(".form-control")
inputs.forEach((event) => {
	event.addEventListener("input", (e) => {
		var message = e.target.parentNode.querySelector(".message")
		if (e.target.value.length > 0) {
			message.innerHTML = ''
		}
	})
})

handleContainer() // default
function handleContainer() {
	// thay đổi container
	if (window.innerWidth <= 992) {
		var containerCart = document.querySelector("#containerCart")
		containerCart.setAttribute("class", "container-fluid px-0")
	} else {
		var containerCart = document.querySelector("#containerCart")
		containerCart.setAttribute("class", "container px-0")
	}

	// thay đổi giao diện sản phẩm giỏ hàng
	if (window.innerWidth <= 340) {
		var productCart = document.querySelectorAll(".product-cart")
		var action = document.querySelectorAll(".action")

		productCart.forEach((event) => {
			event.style = "display: grid; grid-template-columns: auto auto;"
		})
		action.forEach((event) => {
			event.style = "grid-column-end: 3"
		})
	} else {
		var productCart = document.querySelectorAll(".product-cart")
		var action = document.querySelectorAll(".action")

		productCart.forEach((event) => {
			event.style = ""
		})
		action.forEach((event) => {
			event.style = ""
		})
	}
}

window.addEventListener("resize", handleContainer)

var productCart = document.querySelectorAll(".product-cart")
var btnOrder = document.querySelector(".btnOrder")
if (productCart.length <= 0) {
	btnOrder.disabled = true
	btnOrder.style.opacity = '0.5'
}

// thay đổi số lượng sản phẩm trong giỏ hàng
changeQuantityCartP()
function changeQuantityCartP() {
	var qtity = document.querySelectorAll(".qtity")
	qtity.forEach((event) => {
		event.addEventListener("click", (e) => {
			e.preventDefault()
		})
	})
}

plusCartP()
function plusCartP() {
	var btnPlus = document.querySelectorAll(".plus")
	btnPlus.forEach((event) => {
		event.addEventListener("click", (e) => {
			var messageCart = e.target.parentNode.parentNode.parentNode.parentNode.querySelector(".messageCart")
			if (messageCart) {
				messageCart.remove();
			}

			var totalP = e.target.parentNode.parentNode.parentNode.querySelector(".totalP")
			var price = e.target.parentNode.parentNode.parentNode.querySelector(".price")
			var productId = e.target.parentNode.parentNode.id
			var quantity = e.target.parentNode.parentNode.querySelector(".quantity")

			if (productId == '') {
				productId = e.target.parentNode.parentNode.parentNode.id
				price = e.target.parentNode.parentNode.parentNode.parentNode.querySelector(".price")
			}
			quantity.innerHTML = Number(quantity.innerHTML) + 1

			$.ajax({
				url: "/plus-shopping-cart",
				type: "get",
				data: {
					productId: productId,
					quantity: quantity.innerHTML
				},
				success: function(response) {
					var body = response.split("|")

					if (body[0] == 'overload') {
						message("Đã vượt quá số lượng trong kho", "warming")
						quantity.innerHTML = body[1]
						var sumP = price.innerHTML.replaceAll(".", "").replaceAll('đ', '')
						var sum = Number(sumP) * quantity.innerHTML
						totalP.innerHTML = `${sum.toLocaleString('vi-VN')}đ`
					} else {
						var sumP = price.innerHTML.replaceAll(".", "").replaceAll('đ', '')
						var sum = Number(sumP) * quantity.innerHTML
						totalP.innerHTML = `${sum.toLocaleString('vi-VN')}đ`
						totalPay()
					}
				}
			})
		})
	})
}

minusCartP()
function minusCartP() {
	var btnMinus = document.querySelectorAll(".minus")
	btnMinus.forEach((event) => {
		event.addEventListener("click", (e) => {
			var messageCart = e.target.parentNode.parentNode.parentNode.parentNode.querySelector(".messageCart")
			if (messageCart) {
				messageCart.remove();
			}

			var totalP = e.target.parentNode.parentNode.parentNode.querySelector(".totalP")
			var price = e.target.parentNode.parentNode.parentNode.querySelector(".price")
			var productId = e.target.parentNode.parentNode.id
			var quantity = e.target.parentNode.parentNode.querySelector(".quantity")

			if (productId == '') {
				productId = e.target.parentNode.parentNode.parentNode.id
				price = e.target.parentNode.parentNode.parentNode.parentNode.querySelector(".price")
			}

			if (Number(quantity.innerHTML) <= 1) {
				quantity.innerHTML = 1
			} else {
				quantity.innerHTML = Number(quantity.innerHTML) - 1
			}

			$.ajax({
				url: "/minus-shopping-cart",
				type: "get",
				data: {
					productId: productId,
					quantity: quantity.innerHTML
				},
				success: function() {
					var sumP = price.innerHTML.replaceAll(".", "").replaceAll('đ', '')
					var sum = Number(sumP) * quantity.innerHTML
					totalP.innerHTML = `${sum.toLocaleString('vi-VN')}đ`
					totalPay()
				}
			})
		})
	})
}

totalPay()
function totalPay() {
	var sum = 0
	var total = document.querySelector(".total-pay")
	var totalP = document.querySelectorAll(".totalP")

	for (let i = 0; i < totalP.length; i++) {
		var sumP = totalP[i].innerHTML.replaceAll(".", "").replaceAll('đ', '')
		sum += Number(sumP)
	}
	total.innerHTML = `Tổng tiền: ${sum.toLocaleString('vi-VN')}đ`
}

// xóa sản phẩm khỏi giỏ hàng
removeCartP()
function removeCartP() {
	var removes = document.querySelectorAll(".remove")
	var sizeCart = document.querySelectorAll(".sizeCart")

	removes.forEach((event) => {
		event.addEventListener("click", (e) => {
			e.preventDefault()
			var product = e.target.parentNode
			var productId = e.target.id
			if (productId == '') {
				product = e.target.parentNode.parentNode
				productId = e.target.parentNode.id
			}

			swal({
				text: "Bạn chắc muốn bỏ sản phẩm này ra khỏi giỏ hàng?",
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/delete-cartP",
							type: "post",
							data: {
								productId: productId
							},
							success: function(response) {
								product.remove()
								sizeCart.forEach((event) => {
									event.innerHTML = response
								})
								totalPay()

								var productCart = document.querySelectorAll(".product-cart")
								var btnOrder = document.querySelector(".btnOrder")
								if (productCart.length <= 0) {
									btnOrder.disabled = true
									btnOrder.style.opacity = '0.5'
									document.querySelector(".not-cart").innerHTML = 'Bạn chưa có sản phẩm nào trong giỏ hàng !'
								}
							}
						})
					}
				})
		})
	})
}