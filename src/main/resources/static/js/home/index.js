$(document).ready(function() {
	$('.slide-first').remove();
	document.querySelector(".containerSlide").style.display = 'block'

	$('.slides').slick({
		arrows: true,
		dots: true,
		pauseOnFocus: false,
		pauseOnHover: false,
		autoplay: true,
		autoplaySpeed: 3000,
	});
});

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
		var containerHome = document.querySelector("#containerHome")
		containerHome.setAttribute("class", "container-fluid px-0")
	} else {
		var containerHome = document.querySelector("#containerHome")
		containerHome.setAttribute("class", "container px-0")
	}
}

window.addEventListener("resize", handleAddCart)

// paging featured categories
pagingFeaturedCategories()
function pagingFeaturedCategories() {
	var pageNumber = 1;
	var categoryFeatured = document.querySelector(".category-featured")
	var row = categoryFeatured.querySelector(".row")
	var btnViewMore = categoryFeatured.querySelector(".btnViewMore")

	if (btnViewMore) {
		btnViewMore.addEventListener("click", () => {
			btnViewMore.innerHTML = "Đang tải..."
			pageNumber += 1;
			$.ajax({
				url: "/featured-categories",
				type: "get",
				data: {
					page: pageNumber,
				},
				success: function(response) {
					btnViewMore.innerHTML = `Xem thêm sản phẩm <i class="fa-solid fa-angle-down"></i>`
					var body = response.split("|")
					row.innerHTML += body[0]

					var lengthC = row.querySelectorAll("a").length
					if (lengthC) {
						if (lengthC >= body[1]) {
							btnViewMore.remove()
						}
					}
				}
			})
		})
	}
}

// paging products promotion
pagingPromotionProducts()
function pagingPromotionProducts() {
	var pageNumber = 1;
	var promotionProducts = document.querySelector(".promotion-products")
	var row = promotionProducts.querySelector(".row")
	var btnViewMore = promotionProducts.querySelector(".btnViewMore")

	if (btnViewMore) {
		btnViewMore.addEventListener("click", () => {
			btnViewMore.innerHTML = "Đang tải..."
			pageNumber += 1;
			$.ajax({
				url: "/products-promotion",
				type: "get",
				data: {
					page: pageNumber,
				},
				success: function(response) {
					btnViewMore.innerHTML = `Xem thêm sản phẩm <i class="fa-solid fa-angle-down"></i>`
					var body = response.split("|")
					row.innerHTML += body[0]
					addCart()

					var lengthP = row.querySelectorAll(".product").length
					if (lengthP >= body[1]) {
						btnViewMore.remove()
					}
				}
			})
		})
	}
}

// paging products suggest
pagingSuggestProducts()
function pagingSuggestProducts() {
	var pageNumber = 1;
	var suggestProducts = document.querySelector(".suggest-products")
	var row = suggestProducts.querySelector(".row")
	var btnViewMore = suggestProducts.querySelector(".btnViewMore")

	if (btnViewMore) {
		btnViewMore.addEventListener("click", () => {
			btnViewMore.innerHTML = "Đang tải..."
			pageNumber += 1;

			$.ajax({
				url: "/suggest-products",
				type: "get",
				data: {
					page: pageNumber,
				},
				success: function(response) {
					btnViewMore.innerHTML = `Xem thêm sản phẩm <i class="fa-solid fa-angle-down"></i>`
					var body = response.split("|")
					row.innerHTML += body[0]
					addCart()

					if (body[1] == 'end') {
						btnViewMore.remove()
					}
				}
			})
		})
	}
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

// add cart
addCart()
function addCart() {
	var addCarts = document.querySelectorAll(".add-cart")
	var sizeCart = document.querySelectorAll(".sizeCart")
	addCarts.forEach((event, index) => {
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