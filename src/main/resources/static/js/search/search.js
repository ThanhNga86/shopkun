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
		var containerSearch = document.querySelector("#containerSearch")
		containerSearch.setAttribute("class", "container-fluid px-0")
	} else {
		var containerSearch = document.querySelector("#containerSearch")
		containerSearch.setAttribute("class", "container px-0")
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