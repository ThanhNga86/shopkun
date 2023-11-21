var instruction = document.querySelector(".instruction")
instruction.innerHTML = instruction.innerText

handleAddCart() // default
function handleAddCart() {
	var addCart = document.querySelectorAll(".add-cart")
	var price = document.querySelectorAll(".product-price")

	if (window.innerWidth < 320) {
		for (let i = 0; i < addCart.length; i++) {
			addCart[i].innerHTML = '<i class="fa-solid fa-cart-shopping"></i> Thêm'
		}

		for (let i = 0; i < price.length; i++) {
			price[i].style.fontSize = "medium"
		}
	} else {
		for (let i = 0; i < addCart.length; i++) {
			addCart[i].innerHTML = '<i class="fa-solid fa-cart-shopping"></i> Thêm vào giỏ'
		}

		for (let i = 0; i < price.length; i++) {
			price[i].style.fontSize = "large"
		}
	}

	// thay đổi container
	if (window.innerWidth <= 992) {
		var containerCategory = document.querySelector("#containerCategory")
		containerCategory.setAttribute("class", "container-fluid px-0")
	} else {
		var containerCategory = document.querySelector("#containerCategory")
		containerCategory.setAttribute("class", "container px-0")
	}
}

window.addEventListener("resize", handleAddCart)

// add cart
addCart()
function addCart() {
	var addCarts = document.querySelectorAll(".add-cart")
	var sizeCart = document.querySelectorAll(".sizeCart")
	addCarts.forEach((event) => {
		event.addEventListener("click", e => {
			e.preventDefault()
			var productId = e.target.id

			$.ajax({
				url: "/add-cart",
				type: "get",
				data: {
					productId: productId
				},
				success: function(response) {
					if (response != 'not login') {
						sizeCart.forEach((event) => {
							event.innerHTML = response
						})
					} else {
						location.href = '/account/login'
					}
				}
			})
		})
	})
}

// paging products cookie
pagingCookieProducts()
function pagingCookieProducts() {
	var pageNumber = 1;
	var cookieProducts = document.querySelector(".cookie-products")
	if (cookieProducts) {
		var row = cookieProducts.querySelector(".row")
		var btnViewMore = cookieProducts.querySelector(".btnViewMore")

		if (btnViewMore) {
			btnViewMore.addEventListener("click", () => {
				btnViewMore.innerHTML = "Đang tải..."
				pageNumber += 1;

				$.ajax({
					url: "/cookie-products",
					type: "get",
					data: {
						page: pageNumber,
					},
					success: function(response) {
						btnViewMore.innerHTML = `Xem thêm sản phẩm <i class="fa-solid fa-angle-down"></i>`
						var body = response.split("|")
						row.innerHTML += body[0]
						addCart()
						deleteCookieP()

						if (body[1] == 'end') {
							btnViewMore.remove()
						}
					}
				})
			})
		}
	}
}

// delete cookie product
deleteCookieP()
function deleteCookieP() {
	var deleteCookies = document.querySelectorAll(".delete-cookie")
	deleteCookies.forEach((event) => {
		event.addEventListener("click", e => {
			e.preventDefault()
			var product = e.target.parentNode.parentNode.parentNode
			var productId = e.target.id
			if (productId == '') {
				productId = e.target.parentNode.id
				product = product.parentNode
			}

			$.ajax({
				url: "/delete-cookieP",
				type: "post",
				data: {
					productId: productId
				},
				success: function(response) {
					product.remove()

					var cookieProduct = document.querySelector(".cookie-products")
					var lengthP = cookieProduct.querySelectorAll(".product").length

					var totalCookieP = Number(response) - 1
					if (totalCookieP <= 6) {
						var viewMore = cookieProduct.querySelector(".viewMore")
						if (viewMore) {
							viewMore.remove()
						}
					}

					if (lengthP <= 0) {
						cookieProduct.remove()
					}
				}
			})
		})
	})
}