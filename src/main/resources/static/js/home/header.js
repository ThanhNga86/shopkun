// loading...
$(document).ready(function() {
	$('.loading').fadeOut() // mờ dần đi
})

handleScroll() // default
function handleScroll() {
	var logoHome = document.querySelector(".navbar-nav")
	var formSearch = document.querySelector("#formSearch")
	var mdFormSearch = document.querySelector("#md-formSearch")
	var mdUserCart = document.querySelector("#md-user-cart")
	var mdAccount = mdUserCart.querySelector(".account")

	// hiện ẩn menu
	if (window.innerWidth <= 576) { // col-sm-x
		var valueScroll = window.scrollY
		if (valueScroll > 200) {
			logoHome.style.display = "none"
			formSearch.style.display = "none"
			mdAccount.style.display = "none"

			mdFormSearch.style.display = "block"
			mdFormSearch.style.width = "100%"
			mdUserCart.style.display = "block"
		} else {
			logoHome.style.display = "block"
			formSearch.style.display = "block"
			mdAccount.style.display = "block"
			mdUserCart.style = "display: flex; align-items: center;"

			mdFormSearch.style.display = "none"
		}
	} else {
		logoHome.style.display = "block"
		formSearch.style.display = "block"
		mdAccount.style.display = "block"

		mdFormSearch.style.display = "none"
		mdUserCart.style.display = "none"
	}

	// thay đổi container
	if (window.innerWidth <= 992) {
		var navbar = document.querySelector(".navbar")
		var containerFluid = navbar.querySelector(".container")
		if (containerFluid) {
			containerFluid.setAttribute("class", "container-fluid px-1")
		}
	} else {
		var navbar = document.querySelector(".navbar")
		var container = navbar.querySelector(".container-fluid")
		if (container) {
			container.setAttribute("class", "container px-1")
		}
	}
}

window.addEventListener("scroll", handleScroll)
window.addEventListener("resize", handleScroll)

// tìm kiếm sản phẩm
var searchHistory = document.querySelector(".search-history")
var mdSearchHistory = document.querySelector(".md-search-history")

searchProduct()
function searchProduct() {
	var searchRS = document.querySelector(".searchRS")
	var mdSearchRS = document.querySelector(".md-searchRS")
	var inpSearch = document.querySelectorAll(".inpSearch")
	var btnSearch = document.querySelectorAll(".btnSearch")

	inpSearch.forEach((event) => {
		// khóa button nếu value rỗng
		btnSearch.forEach((btn) => {
			if (event.value == '') {
				btn.disabled = true
			}
		})

		event.addEventListener("focus", (e) => {
			var query = e.target.value

			if (event.value == '') {
				if (searchHistory) {
					searchHistory.style.display = 'block'
				}
				if (mdSearchHistory) {
					mdSearchHistory.style.display = 'block'
				}
			} else {
				if (searchRS) {
					searchRS.style.display = "block"
				}
				if (mdSearchRS) {
					mdSearchRS.style.display = "block"
				}

				$.ajax({
					url: "/search",
					type: "post",
					data: {
						q: query
					},
					success: function(response) {
						searchRS.innerHTML = response
						mdSearchRS.innerHTML = response
					}
				})
			}
		})
	})

	inpSearch.forEach((event) => {
		event.addEventListener("blur", () => {
			setTimeout(() => {
				if (searchHistory) {
					searchHistory.style.display = 'none'
				}
				if (mdSearchHistory) {
					mdSearchHistory.style.display = 'none'
				}

				if (searchRS) {
					searchRS.style.display = "none"
				}
				if (mdSearchRS) {
					mdSearchRS.style.display = "none"
				}
			}, 100)
		})
	})

	inpSearch.forEach((event) => {
		event.addEventListener("input", (e) => {
			// mở khóa button nếu value ko rỗng
			btnSearch.forEach((btn) => {
				if (e.target.value != '') {
					btn.disabled = false
				} else {
					btn.disabled = true
				}
			})
			
			var query = e.target.value

			if (query != '') {
				if (searchHistory) {
					searchHistory.style.display = 'none'
				}
				if (mdSearchHistory) {
					mdSearchHistory.style.display = 'none'
				}

				searchRS.style.display = 'block'
				searchRS.innerHTML = `
					<div class="loadTab text-center">
						<div class="lds-dual-ring"></div>
					</div>
				`
				mdSearchRS.style.display = 'block'
				mdSearchRS.innerHTML = `
					<div class="loadTab text-center">
						<div class="lds-dual-ring"></div>
					</div>
				`

				$.ajax({
					url: "/search",
					type: "post",
					data: {
						q: query
					},
					success: function(response) {
						searchRS.innerHTML = response
						mdSearchRS.innerHTML = response
					}
				})
			} else {
				searchRS.style.display = "none"
				mdSearchRS.style.display = "none"
			}
		})
	})
}

// xóa lịch sử tìm kiếm
var removeH = document.querySelectorAll(".removeH")
removeH.forEach((event) => {
	event.addEventListener("click", (e) => {
		e.preventDefault()
		var resultSearch = e.target.parentNode.parentNode
		var searchHistory = resultSearch.parentNode
		var query = resultSearch.querySelector("span").innerText

		$.ajax({
			url: "/delete-search-history",
			type: "get",
			data: {
				q: query
			},
			success: function() {
				resultSearch.remove()

				var lengthQ = searchHistory.querySelectorAll(".rs").length
				if (lengthQ == 0) {
					searchHistory.remove()
				}
			}
		})

		var inpSearch = document.querySelectorAll(".inpSearch")
		setTimeout(() => {
			searchHistory.style.display = 'none'
			mdSearchHistory.style.display = 'none'
		}, 10)
		inpSearch.forEach((event) => {
			setTimeout(() => {
				searchHistory.style.display = 'block'
				mdSearchHistory.style.display = 'block'
				event.focus()
			}, 102)
		})
	})
})