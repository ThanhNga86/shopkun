// cho <br> xuống hàng
commentBR()
function commentBR() {
	var commentBR = document.querySelectorAll(".comment")
	commentBR.forEach((event) => {
		event.innerHTML = event.innerText
	})
	var cmtFB = document.querySelectorAll(".cmtFB")
	cmtFB.forEach((event) => {
		event.innerHTML = event.innerText
	})
}

// hiện và tắt chế độ xem nhiều hình ảnh
showAndHiddenImages()
function showAndHiddenImages() {
	var viewDetailImage = document.querySelectorAll(".viewDetailImage")
	for (let i = 0; i < viewDetailImage.length; i++) {
		viewDetailImage[i].addEventListener("click", () => {
			showHiddenImage("show", i)
		})
	}

	var closeImages = document.querySelectorAll(".hidden-detailImage")
	for (let i = 0; i < closeImages.length; i++) {
		closeImages[i].addEventListener("click", () => {
			showHiddenImage("hidden", i)
		})
	}

	var showImages = document.querySelectorAll(".show-images")
	for (let i = 0; i < showImages.length; i++) {
		showImages[i].addEventListener("click", (e) => {
			if (e.target.classList.contains('row')) {
				showHiddenImage("hidden", i)
			}
		})
	}

	var showDetailImages = document.querySelectorAll(".show-detailImage")
	for (let i = 0; i < showDetailImages.length; i++) {
		showDetailImages[i].addEventListener("click", () => {
			showHiddenImage("hidden", i)
		})
	}

	function showHiddenImage(action, i) {
		var showDetailImage = document.querySelectorAll(".show-detailImage")
		var showImage = document.querySelectorAll(".show-images")

		if (action == 'show') {
			showDetailImage[i].style.display = 'block'
			showImage[i].style.display = 'block'
			document.body.style.overflow = 'hidden'
		} else {
			showDetailImage[i].style.display = 'none'
			showImage[i].style.display = 'none'
			document.body.style.overflow = 'auto'
		}
	}
}

// hiện và tắt chế độ xem chi tiết đánh giá
showAndHiddenReviewDetail()
function showAndHiddenReviewDetail() {
	var viewDetail = document.querySelectorAll(".see")
	for (let i = 0; i < viewDetail.length; i++) {
		viewDetail[i].addEventListener("click", (e) => {
			showHiddenReviewD("show", i)
		})
	}

	var closeX = document.querySelectorAll(".hidden-reviewDetail")
	for (let i = 0; i < closeX.length; i++) {
		closeX[i].addEventListener("click", () => {
			showHiddenReviewD("hidden", i)
		})
	}

	var showDetail = document.querySelectorAll(".show-reviewDetails")
	for (let i = 0; i < showDetail.length; i++) {
		showDetail[i].addEventListener("click", (e) => {
			if (e.target.classList.contains('row')) {
				showHiddenReviewD("hidden", i)
			}
		})
	}

	var showDetails = document.querySelectorAll(".show-reviewDetail")
	for (let i = 0; i < showDetails.length; i++) {
		showDetails[i].addEventListener("click", () => {
			showHiddenReviewD("hidden", i)
		})
	}

	function showHiddenReviewD(action, i) {
		var showDetail = document.querySelectorAll(".show-reviewDetail")
		var showDetails = document.querySelectorAll(".show-reviewDetails")

		if (action == 'show') {
			showDetail[i].style.display = 'block'
			showDetails[i].style.display = 'block'
			document.body.style.overflow = 'hidden'
		} else {
			showDetail[i].style.display = 'none'
			showDetails[i].style.display = 'none'
			document.body.style.overflow = 'auto'
		}
	}
}

// xóa đánh giá
removeReview()
function removeReview() {
	var remove = document.querySelectorAll(".remove")
	for (let i = 0; i < remove.length; i++) {
		remove[i].addEventListener("click", () => {
			var reviewId = remove[i].id
			var row = remove[i].parentNode.parentNode

			swal({
				text: `Bạn chắc muốn xóa đánh giá này?`,
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/admin/delete-review",
							type: "get",
							data: {
								reviewId: reviewId
							},
							success: function(response) {
								if (response == 'success') {
									swal("", "Xóa đánh giá thành công", "success")
									row.remove()
								}
								else if (response == 'error') {
									swal("", "Đánh giá này không thể xóa", "error")
								}
							}
						})
					}
				})
		})
	}
}

// xóa phản hồi đánh giá
removeReviewFB()
function removeReviewFB() {
	var remove = document.querySelectorAll(".removeFB")
	for (let i = 0; i < remove.length; i++) {
		remove[i].addEventListener("click", () => {
			var reviewFBId = remove[i].id
			var row = remove[i].parentNode.parentNode

			swal({
				text: `Bạn chắc muốn xóa phản hồi đánh giá này?`,
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/admin/delete-reviewFB",
							type: "get",
							data: {
								reviewFBId: reviewFBId
							},
							success: function(response) {
								if (response == 'success') {
									swal("", "Xóa phản hồi đánh giá thành công", "success")
									row.remove()
								}
								else if (response == 'error') {
									swal("", "Phàn hồi đánh giá này không thể xóa", "error")
								}
							}
						})
					}
				})
		})
	}
}

// tìm kiếm đánh giá theo mã số, bình luận, mã sản phẩm
var vlSearch = sessionStorage.getItem("vlSearch")
if (vlSearch) {
	var tableReview = document.querySelector(".tableReview")
	var searchR = document.querySelector(".searchR")
	var paging = document.querySelector("#paging")
	var dataFilter = document.querySelector(".btnFilter").getAttribute("data-filter")
	searchR.value = vlSearch

	tableReview.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

	if (paging) {
		if (searchR.value == '') {
			paging.style.display = "flex"
		} else {
			paging.style.display = "none"
		}
	}

	$.ajax({
		url: "/admin/search-review",
		type: "get",
		data: {
			query: searchR.value,
			dataFilter: dataFilter
		},
		success: function(response) {
			sessionStorage.setItem("vlSearch", searchR.value)
			tableReview.innerHTML = response

			commentBR()
			showAndHiddenImages()
			showAndHiddenReviewDetail()
			removeReview()
			removeReviewFB()
		}
	})
}

searchAndFilterReview()
function searchAndFilterReview() {
	var tableReview = document.querySelector(".tableReview")
	var searchR = document.querySelector(".searchR")
	var paging = document.querySelector("#paging")
	var dataFilter = document.querySelector(".btnFilter").getAttribute("data-filter")

	searchR.addEventListener("input", () => {
		tableReview.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

		if (paging) {
			if (searchR.value == '') {
				paging.style.display = "flex"
			} else {
				paging.style.display = "none"
			}
		}

		setTimeout(() => {
			$.ajax({
				url: "/admin/search-review",
				type: "get",
				data: {
					query: searchR.value,
					dataFilter: dataFilter
				},
				success: function(response) {
					sessionStorage.setItem("vlSearch", searchR.value)
					tableReview.innerHTML = response

					commentBR()
					showAndHiddenImages()
					showAndHiddenReviewDetail()
					removeReview()
					removeReviewFB()
				}
			})
		}, 500)
	})
}