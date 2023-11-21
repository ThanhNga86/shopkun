var instruction = document.querySelector(".instruction")
instruction.innerHTML = instruction.innerText

$(document).ready(function() {
	$('.image-first').remove();
	document.querySelector(".images").style.display = 'block'

	$('.images').slick({
		arrows: true,
		pauseOnFocus: false,
		pauseOnHover: false,
	});
});

handleContainer() // default
function handleContainer() {
	// thay đổi container
	if (window.innerWidth <= 992) {
		var containerProduct = document.querySelector("#containerProduct")
		containerProduct.setAttribute("class", "container-fluid px-0")
	} else {
		var containerProduct = document.querySelector("#containerProduct")
		containerProduct.setAttribute("class", "container px-0")
	}
}

window.addEventListener("resize", handleContainer)

$(document).ready(function() {
	var similarProducts = document.querySelector(".similarProducts")
	if (similarProducts) {
		similarProducts.style.display = 'block'

		$('.similarProducts').slick({
			arrows: true,
			pauseOnFocus: false,
			pauseOnHover: false,
			infinite: false,
			slidesToShow: 5,
			slidesToScroll: 5,
			responsive: [
				{
					breakpoint: 1200,
					settings: {
						slidesToShow: 3,
						slidesToScroll: 3,
					}
				},
				{
					breakpoint: 600,
					settings: {
						slidesToShow: 2,
						slidesToScroll: 2
					}
				},
				{
					breakpoint: 480,
					settings: {
						slidesToShow: 1,
						slidesToScroll: 1
					}
				}
			]
		});
	}
});

var cookieProducts = document.querySelector(".cookieProducts");
if (cookieProducts) {
	$(document).ready(function() {
		cookieProducts.style.display = 'block'

		$('.cookieProducts').slick({
			arrows: true,
			pauseOnFocus: false,
			pauseOnHover: false,
			infinite: false,
			slidesToShow: 5,
			slidesToScroll: 5,
			responsive: [
				{
					breakpoint: 1200,
					settings: {
						slidesToShow: 3,
						slidesToScroll: 3,
					}
				},
				{
					breakpoint: 600,
					settings: {
						slidesToShow: 2,
						slidesToScroll: 2
					}
				},
				{
					breakpoint: 480,
					settings: {
						slidesToShow: 1,
						slidesToScroll: 1
					}
				}
			]
		});
	});
}

// add cart
addCartP()
function addCartP() {
	var shopingCartP = document.querySelector(".shopping-cartP")
	if (shopingCartP) {
		// giảm số lượng
		var minus = document.querySelector(".minus")
		minus.addEventListener("click", () => {
			var quantity = minus.parentNode.querySelector(".quantity")

			if (Number(quantity.innerHTML) <= 1) {
				quantity.innerHTML = 1
			} else {
				quantity.innerHTML = Number(quantity.innerHTML) - 1
			}
		})

		// tăng số lượng
		var plus = document.querySelector(".plus")
		plus.addEventListener("click", () => {
			var productId = plus.parentNode.parentNode.querySelector(".add-cart").id
			var quantity = plus.parentNode.querySelector(".quantity")
			quantity.innerHTML = Number(quantity.innerHTML) + 1

			$.ajax({
				url: "/plus-cart",
				type: "get",
				data: {
					productId: productId,
					quantity: quantity.innerHTML
				},
				success: function(response) {
					if (response == 'overload') {
						message("Đã vượt quá số lượng trong kho", "warming")
						quantity.innerHTML = Number(quantity.innerHTML) - 1
					}
				}
			})
		})

		// thêm vào giỏ
		var addCart = document.querySelector(".add-cart")
		addCart.addEventListener("click", (e) => {
			var quantity = document.querySelector(".quantity")
			var productId = e.target.id
			if (e.target.classList.contains('fa-cart-plus')) {
				productId = e.target.parentNode.id
			}
			var sizeCart = document.querySelectorAll(".sizeCart")

			$.ajax({
				url: "/add-cartP",
				type: "get",
				data: {
					productId: productId,
					quantity: quantity.innerHTML
				},
				success: function(response) {
					if (response != 'not login') {
						if (response != 'overload') {
							sizeCart.forEach((event) => {
								event.innerHTML = response
							})
						} else {
							message("Đã vượt quá số lượng trong kho", "warming")
						}
					} else {
						location.href = '/account/login'
					}
				}
			})
		})

		// đặt mua
		var btnOrder = document.querySelector(".btnOrder")
		btnOrder.addEventListener("click", (e) => {
			var quantity = document.querySelector(".quantity")
			var productId = e.target.id
			if (e.target.classList.contains('fa-cart-shopping')) {
				productId = e.target.parentNode.id
			}
			var sizeCart = document.querySelectorAll(".sizeCart")

			$.ajax({
				url: "/add-cartP",
				type: "get",
				data: {
					productId: productId,
					quantity: quantity.innerHTML
				},
				success: function(response) {
					if (response != 'not login') {
						if (response != 'overload') {
							location.href = '/shopping-cart'
						} else {
							message("Đã vượt quá số lượng trong kho", "warming")
						}
					} else {
						location.href = '/account/login'
					}
				}
			})
		})
	}
}

// Hidden-Show detail images
function showHiddenImages(action) {
	var showDetailImage = document.querySelector(".show-detailImage")
	var showImage = document.querySelector(".show-images")

	if (action == 'show') {
		showDetailImage.style.display = 'block'
		showImage.style.display = 'block'
		document.body.style.overflow = 'hidden'
	} else {
		showDetailImage.style.display = 'none'
		showImage.style.display = 'none'
		document.body.style.overflow = 'auto'
	}
}

var detailImage = document.querySelector(".detail-image")
detailImage.addEventListener("click", () => {
	showHiddenImages('show')
})

var closeImages = document.querySelector("#hidden-detailImage")
closeImages.addEventListener("click", () => {
	showHiddenImages('hidden')
})

var showImages = document.querySelector(".show-images")
showImages.addEventListener("click", (e) => {
	if (e.target.classList.contains('row')) {
		showHiddenImages('hidden')
	}
})

var showDetailImages = document.querySelector(".show-detailImage")
showDetailImages.addEventListener("click", () => {
	showHiddenImages('hidden')
})

// action view reviews and information product
var infoP = document.querySelector(".infoP")
infoP.addEventListener("click", () => {
	var infoProduct = document.querySelector(".info-product")
	window.scrollTo({ top: infoProduct.offsetTop - 45, behavior: "smooth" })
})

var reviews = document.querySelector(".reviews")
var reviewPoint = document.querySelector(".review-point")
if (reviews) {
	reviews.addEventListener("click", () => {
		var reviewP = document.querySelector(".review-product")
		window.scrollTo({ top: reviewP.offsetTop - 45, behavior: "smooth" })
		console.log(reviewP.offsetTop - 45)
	})
}
if (reviewPoint) {
	reviewPoint.addEventListener("click", () => {
		var reviewP = document.querySelector(".review-product")
		window.scrollTo({ top: reviewP.offsetTop - 45, behavior: "smooth" })
	})
}

// point review star
var rateStar = (document.querySelector(".point-rv")) ? Number(document.querySelector(".point-rv").innerHTML) : 5
var point = document.querySelector(".point")
if (point) {
	rateStarReview(rateStar, point)
}
function rateStarReview(rate, inner) {
	var rsRate = (Math.floor(rate + 1) - rate) * 100
	for (let i = 0; i < Math.floor(rate); i++) {
		inner.innerHTML += '<i class="fa-solid fa-star"></i>'
	}

	var starLength = inner.querySelectorAll(".fa-star").length - 1
	var star = inner.querySelectorAll(".fa-star")[starLength]
	if (star) {
		if (rsRate != 100) {
			star.style = `background: linear-gradient(to right, orange ${100 - rsRate}%, lightgray ${rsRate}%);
  				  -webkit-background-clip: text;
  				  background-clip: text;
  				  color: transparent;`
		} else {
			star.style.color = 'orange'
		}
	}
}

var starReview = document.querySelector(".star-rv")
if (starReview) {
	rateStarReview(rateStar, starReview)
}

var starReview = document.querySelector(".level-rv")
if (starReview) {
	if (rateStar >= 1 && rateStar < 2) {
		starReview.innerHTML = 'Đánh giá kém'
	}
	else if (rateStar >= 2 && rateStar < 3) {
		starReview.innerHTML = 'Đánh giá tạm được'
	}
	else if (rateStar >= 3 && rateStar < 4) {
		starReview.innerHTML = 'Đánh giá trung bình'
	}
	else if (rateStar >= 4 && rateStar < 5) {
		starReview.innerHTML = 'Đánh giá tốt'
	}
	else if (rateStar >= 5) {
		starReview.innerHTML = 'Đánh giá tuyệt vời'
	}
}

// Comment product
var btnReview = document.querySelector("#btnReview")
showStarCmt()
function showStarCmt() {
	var starCmt = document.querySelectorAll(".star-cmt")
	starCmt.forEach((event) => {
		var numbStar = Math.floor(event.id)
		event.innerHTML = ''
		for (let i = 0; i < numbStar; i++) {
			event.innerHTML += '<i class="fa-solid fa-star"></i>'
		}
	})
}

var numberStar = 5
reviewByStar(numberStar)
function reviewByStar(numberStar) {
	var stars = document.querySelectorAll(".star")

	// default
	for (let i = 0; i < numberStar; i++) {
		stars[i].querySelector(".fa-star").style.color = 'orange'
	}
	stars[numberStar - 1].style.color = 'deepskyblue'

	// review by star
	for (let i = 0; i < stars.length; i++) {
		stars[i].addEventListener("click", () => {
			numberStar = i;
			for (let j = 0; j <= i; j++) {
				stars[j].querySelector(".fa-star").style.color = 'orange'
			}

			for (let j = i + 1; j < stars.length; j++) {
				stars[j].querySelector(".fa-star").style.color = 'lightgray'
			}

			stars[i].style.color = 'deepskyblue'
			for (let j = 0; j < stars.length; j++) {
				if (j != i) {
					stars[j].style.color = 'black'
				}
			}
		})
	}
}
var stars = document.querySelectorAll(".star")
stars.forEach((event, index) => {
	event.addEventListener("click", () => {
		numberStar = index + 1
	})
})

// Send review
var sendFile = document.querySelector("#sendFile")
var showFiles = document.querySelector(".show-files")
var file = document.querySelector("#file")
sendFile.addEventListener("click", () => {
	file.click()
})

var arrFileCmt = []
showImageByCmt(file, showFiles)
function showImageByCmt(file, showFiles) {
	var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
	var maxSize = 1 * 1024 * 1024; // 2 MB
	var flagFile = true
	file.addEventListener("change", function() {
		if (file.files && file.files[0]) {
			for (let i = 0; i < file.files.length; i++) {
				if (file.files[i].size <= maxSize && checkImage.test(file.files[i].name)) {
					var showFile = document.createElement("div");
					showFile.setAttribute("class", 'show-file')
					showFile.setAttribute("id", arrFileCmt.length + 1)
					showFile.innerHTML = `
					<button type="button" class="btn-close" aria-label="Close"></button>
					<img width="100%">
				`
					showFiles.appendChild(showFile);
					showFile.querySelector("img").src = URL.createObjectURL(file.files[i]);
					arrFileCmt.push({
						id: showFile.id,
						files: file.files[i]
					})

					flagFile = true
				} else {
					flagFile = false;
				}
			}

			if (!flagFile) {
				message('Vui lòng chọn một hình ảnh có định dạng hợp lệ và kích thước không quá 1 MB.', "warming");
			}
		}

		var btnClose = document.querySelectorAll(".btn-close")
		btnClose.forEach((event) => {
			event.addEventListener("click", (e) => {
				var showFile = e.target.parentNode
				showFile.remove()
				for (let i = 0; i < arrFileCmt.length; i++) {
					if (arrFileCmt[i].id == showFile.id) {
						arrFileCmt.splice(i, 1)
						break;
					}
				}
			})
		})
	})
}

var reviewCmt = document.querySelector(".review-comment")
// reset lại bình luận cho xuống dòng với <br>
var textCmts = reviewCmt.querySelectorAll(".text-cmt")
for (let i = 0; i < textCmts.length; i++) {
	textCmts[i].innerHTML = textCmts[i].innerText
}

// thay thế \n thành <br> để text đc xuống dòng
var comment = document.querySelector("#comment")
var textCmt = ''
comment.addEventListener("input", () => {
	textCmt = comment.value.replaceAll(/\n/g, "<br>");
});

function sendReview() {
	var inpComment = document.querySelector("#comment")
	var productId = document.querySelector(".product-describe")
	var formData = new FormData()

	formData.set("comment", textCmt)
	formData.set("rate", numberStar)
	formData.set("productId", productId.id)
	for (let i = 0; i < arrFileCmt.length; i++) {
		formData.append("files", arrFileCmt[i].files)
	}

	if (inpComment.value != '' || arrFileCmt.length > 0) {
		btnReview.innerHTML = "Đang gửi..."

		$.ajax({
			url: "/product/review",
			type: "post",
			processData: false,  // Không xử lý dữ liệu
			contentType: false,  // Không cần ghi đè contentType
			data: formData,
			success: function(response) {
				if (response != 'not login') {
					btnReview.innerHTML = "Gửi đánh giá"
					reviewCmt.innerHTML = response
					// hiển thị hình ảnh tạm thời
					var cmt = reviewCmt.querySelectorAll(".cmt")[0]
					var imageCmt = cmt.querySelector(".image-cmt")
					if (imageCmt) {
						imageCmt.innerHTML = ''
						for (let i = 0; i < arrFileCmt.length; i++) {
							var img = document.createElement("img")
							img.width = "100"
							img.height = "100"
							img.src = URL.createObjectURL(arrFileCmt[i].files)
							imageCmt.appendChild(img)
						}
					}

					// reset lại form
					inpComment.value = ''
					textCmt = ''
					showFiles.innerHTML = ''
					file.value = ''
					arrFileCmt = []
					showStarCmt()

					reviewFeedBackCmt()
				} else {
					location.href = '/account/login'
				}
			}
		})
	}

	// paging
	var viewMore = document.querySelector(".viewMore")
	var cmt = document.querySelectorAll(".cmt")
	if (viewMore) {
		if (cmt.length >= 10) {
			viewMore.innerHTML = `<span class="btnViewMore" id="viewCmt">Xem thêm bình luận <i class="fa-solid fa-angle-down"></i></span>`
			pagingReview()
		}
	}
}
btnReview.addEventListener("click", sendReview)

// reset lại bình luận cho xuống dòng với <br>
var feedbacks = document.querySelectorAll(".feedbacks")
for (let i = 0; i < feedbacks.length; i++) {
	var textCmtFBs = feedbacks[i].querySelectorAll(".text-cmtFB")
	for (let j = 0; j < textCmtFBs.length; j++) {
		textCmtFBs[j].innerHTML = textCmtFBs[j].innerText
	}
}

reviewFeedBackCmt()
function reviewFeedBackCmt() {
	// feedback comment
	var arrFB = []
	function showImageByFB(file, showFiles) {
		var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
		var maxSize = 1 * 1024 * 1024; // 2 MB
		var flagFile = true
		file.addEventListener("change", function() {
			if (file.files && file.files[0]) {
				for (let i = 0; i < file.files.length; i++) {
					if (file.files[i].size <= maxSize && checkImage.test(file.files[i].name)) {
						var showFile = document.createElement("div");
						showFile.setAttribute("class", 'show-file')
						showFile.setAttribute("id", arrFB.length + 1)
						showFile.innerHTML = `
					<button type="button" class="btn-close" aria-label="Close"></button>
					<img width="100%">
				`
						showFiles.appendChild(showFile);
						showFile.querySelector("img").src = URL.createObjectURL(file.files[i]);
						arrFB.push({
							id: showFile.id,
							files: file.files[i]
						})
						flagFile = true
					} else {
						flagFile = false;
					}
				}

				if (!flagFile) {
					message('Vui lòng chọn một hình ảnh có định dạng hợp lệ và kích thước không quá 1 MB.', "warming");
				}
			}

			var btnClose = document.querySelectorAll(".btn-close")
			btnClose.forEach((event) => {
				event.addEventListener("click", (e) => {
					var showFile = e.target.parentNode
					showFile.remove()
					for (let i = 0; i < arrFB.length; i++) {
						if (arrFB[i].id == showFile.id) {
							arrFB.splice(i, 1)
							break;
						}
					}
				})
			})
		})
	}

	var textCmtFB = ''
	function feedBackCmt(e) {
		var comment = e.target.parentNode
		var feedback = comment.querySelector(".feedbackCmt")
		var feedbacks = document.querySelectorAll(".feedbackCmt")
		var feedbackFBs = document.querySelectorAll(".feedbackCmtFB")

		for (let i = 0; i < feedbacks.length; i++) {
			feedbacks[i].innerHTML = ''
		}
		for (let i = 0; i < feedbackFBs.length; i++) {
			feedbackFBs[i].innerHTML = ''
		}
		feedback.innerHTML = `
			<div class="row">
				<div class="col-xl-8 col-lg-8 col-md-10 col-sm-10 my-2">
					<textarea name="comment" class="comment form-control" rows="3" placeholder="Viết bình luận / câu hỏi / nhận xét..."></textarea>
					<div class="action p-2 bg-body-secondary">
						<div class="ctn-file">
							<input type="file" name="file" class="file" multiple="multiple" style="display: none;">
							<div class="sendFile">
								<i class="fa-solid fa-camera"></i>
								<span>Gửi ảnh</span>
							</div>
							
						</div>
						<button type="btn" class="btn btnReview">Trả lời</button>
					</div>
					
					<div class="show-files row bg-body-secondary"></div>
				</div>
			</div>
		`
		var cmt = comment.querySelector(".comment")
		cmt.focus()

		var sendFiles = document.querySelectorAll(".sendFile")
		for (let i = 0; i < sendFiles.length; i++) {
			sendFiles[i].addEventListener("click", (e) => {
				var ctnFile = e.target.parentNode.parentNode
				var file = ctnFile.querySelector(".file")
				file.click()
			})
		}

		var file = e.target.parentNode.querySelector(".file")
		var showFile = e.target.parentNode.querySelector(".show-files")
		showImageByFB(file, showFile)

		// thay thế \n thành <br> để text đc xuống dòng
		var commentFB = document.querySelector(".comment")
		commentFB.addEventListener("input", () => {
			textCmtFB = commentFB.value.replaceAll(/\n/g, "<br>");
		});

		var btnReview = document.querySelector(".btnReview")
		btnReview.addEventListener("click", sendFeedBackCmt)
	}

	function sendFeedBackCmt(e) {
		var comment = e.target.parentNode.parentNode.parentNode.parentNode.parentNode
		var cmt = comment.querySelector(".comment")
		var file = comment.querySelector(".file")
		var showFiles = comment.querySelector(".show-files")

		var formData = new FormData()
		formData.set("reviewId", comment.id)
		formData.set("cmt", textCmtFB)
		for (let i = 0; i < arrFB.length; i++) {
			formData.append("files", arrFB[i].files)
		}

		var feedbacks = comment.querySelector(".feedbacks")

		if (cmt.value != '' || arrFB.length > 0) {
			var btnReview = document.querySelector(".btnReview")
			btnReview.innerHTML = "Đang gửi..."

			$.ajax({
				url: "/product/feedback",
				type: "post",
				processData: false,  // Không xử lý dữ liệu
				contentType: false,  // Không cần ghi đè contentType
				data: formData,
				success: function(response) {
					if (response != 'not login') {
						btnReview.innerHTML = "Trả lời"
						feedbacks.innerHTML = response

						// hiển thị lại hình ảnh tạm thời
						var cmtFBs = feedbacks.querySelectorAll(".cmtFB")
						var cmtFB = feedbacks.querySelectorAll(".cmtFB")[cmtFBs.length - 1]
						var imageCmt = cmtFB.querySelector(".image-cmtFB")
						if (imageCmt) {
							imageCmt.innerHTML = ''
							for (let i = 0; i < arrFB.length; i++) {
								var img = document.createElement("img")
								img.width = "100"
								img.height = "100"
								img.src = URL.createObjectURL(arrFB[i].files)
								imageCmt.appendChild(img)
							}
						}

						cmt.value = ''
						textCmtFB = ''
						showFiles.innerHTML = ''
						file.value = ''
						arrFB = []

						var replyFBs = document.querySelectorAll(".reply-cmtFB")
						replyFBs.forEach((event) => {
							event.addEventListener("click", (e) => {
								feedBackInCmt(e)
							})
						})
					} else {
						location.href = '/account/login'
					}
				}
			})
		}
	}

	var replys = document.querySelectorAll(".reply-cmt")
	replys.forEach((event) => {
		event.addEventListener("click", (e) => {
			feedBackCmt(e)
		})
	})

	// reply feedback
	var arrInFB = []
	function showImageByInFB(file, showFiles) {
		var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
		var maxSize = 1 * 1024 * 1024; // 2 MB
		var flagFile = true
		file.addEventListener("change", function() {
			if (file.files && file.files[0]) {
				for (let i = 0; i < file.files.length; i++) {
					if (file.files[i].size <= maxSize && checkImage.test(file.files[i].name)) {
						var showFile = document.createElement("div");
						showFile.setAttribute("class", 'show-file')
						showFile.setAttribute("id", arrInFB.length + 1)
						showFile.innerHTML = `
					<button type="button" class="btn-close" aria-label="Close"></button>
					<img width="100%">
				`
						showFiles.appendChild(showFile);
						showFile.querySelector("img").src = URL.createObjectURL(file.files[i]);
						arrInFB.push({
							id: showFile.id,
							files: file.files[i]
						})
						flagFile = true
					} else {
						flagFile = false;
					}
				}

				if (!flagFile) {
					message('Vui lòng chọn một hình ảnh có định dạng hợp lệ và kích thước không quá 1 MB.', "warming");
				}
			}

			var btnClose = document.querySelectorAll(".btn-close")
			btnClose.forEach((event) => {
				event.addEventListener("click", (e) => {
					var showFile = e.target.parentNode
					showFile.remove()
					for (let i = 0; i < arrInFB.length; i++) {
						if (arrInFB[i].id == showFile.id) {
							arrInFB.splice(i, 1)
							break;
						}
					}
				})
			})
		})
	}

	function feedBackInCmt(e) {
		var comment = e.target.parentNode
		var feedback = comment.querySelector(".feedbackCmtFB")
		var feedbacks = document.querySelectorAll(".feedbackCmt")
		var feedbackFBs = document.querySelectorAll(".feedbackCmtFB")

		for (let i = 0; i < feedbacks.length; i++) {
			feedbacks[i].innerHTML = ''
		}
		for (let i = 0; i < feedbackFBs.length; i++) {
			feedbackFBs[i].innerHTML = ''
		}
		feedback.innerHTML = `
			<div class="row">
				<div class="col-xl-8 col-lg-8 col-md-10 col-sm-10 my-2 px-0">
					<textarea name="comment" class="comment form-control" rows="3" placeholder="Viết bình luận / câu hỏi / nhận xét..."></textarea>
					<div class="action p-2 bg-body-secondary">
						<div class="ctn-file">
							<input type="file" name="file" class="file" multiple="multiple" style="display: none;">
							<div class="sendFile">
								<i class="fa-solid fa-camera"></i>
								<span>Gửi ảnh</span>
							</div>
							
						</div>
						<button type="btn" class="btn btnReview">Trả lời</button>
					</div>
					
					<div class="show-files row bg-body-secondary"></div>
				</div>
			</div>
		`
		var cmt = comment.querySelector(".comment")
		cmt.focus()

		var sendFiles = document.querySelectorAll(".sendFile")
		for (let i = 0; i < sendFiles.length; i++) {
			sendFiles[i].addEventListener("click", (e) => {
				var ctnFile = e.target.parentNode.parentNode
				var file = ctnFile.querySelector(".file")
				file.click()
			})
		}

		var file = e.target.parentNode.querySelector(".file")
		var showFile = e.target.parentNode.querySelector(".show-files")
		showImageByInFB(file, showFile)

		// thay thế \n thành <br> để text đc xuống dòng
		var commentFB = document.querySelector(".comment")
		commentFB.addEventListener("input", () => {
			textCmtFB = commentFB.value.replaceAll(/\n/g, "<br>");
		});

		var btnReview = document.querySelector(".btnReview")
		btnReview.addEventListener("click", sendFeedBackInCmt)
	}

	function sendFeedBackInCmt(e) {
		var comment = e.target.parentNode.parentNode.parentNode.parentNode.parentNode
		var respondent = comment.querySelector(".name-cmtFB")
		var cmt = comment.querySelector(".comment")
		var file = comment.querySelector(".file")
		var showFiles = comment.querySelector(".show-files")

		var formData = new FormData()
		formData.set("respondent", respondent.innerHTML)
		formData.set("reviewId", comment.id)
		formData.set("cmt", textCmtFB)
		for (let i = 0; i < arrInFB.length; i++) {
			formData.append("files", arrInFB[i].files)
		}

		var feedbacks = comment.parentNode.parentNode.querySelector(".feedbacks")

		if (cmt.value != '' || arrInFB.length > 0) {
			var btnReview = document.querySelector(".btnReview")
			btnReview.innerHTML = "Đang gửi..."

			$.ajax({
				url: "/product/feedback-comment",
				type: "post",
				processData: false,  // Không xử lý dữ liệu
				contentType: false,  // Không cần ghi đè contentType
				data: formData,
				success: function(response) {
					if (response != 'not login') {
						btnReview.innerHTML = "Trả lời"
						feedbacks.innerHTML = response
						// hiển thị lại hình ảnh tạm thời
						var cmtFBs = feedbacks.querySelectorAll(".cmtFB")
						var cmtFB = feedbacks.querySelectorAll(".cmtFB")[cmtFBs.length - 1]
						var imageCmt = cmtFB.querySelector(".image-cmtFB")
						if (imageCmt) {
							imageCmt.innerHTML = ''
							for (let i = 0; i < arrInFB.length; i++) {
								var img = document.createElement("img")
								img.width = "100"
								img.height = "100"
								img.src = URL.createObjectURL(arrInFB[i].files)
								imageCmt.appendChild(img)
							}
						}

						cmt.value = ''
						showFiles.innerHTML = ''
						file.value = ''
						arrInFB = []

						var replyFBs = document.querySelectorAll(".reply-cmtFB")
						replyFBs.forEach((event) => {
							event.addEventListener("click", (e) => {
								feedBackInCmt(e)
							})
						})
					} else {
						location.href = '/account/login'
					}
				}
			})
		}
	}

	var replyFBs = document.querySelectorAll(".reply-cmtFB")
	replyFBs.forEach((event) => {
		event.addEventListener("click", (e) => {
			feedBackInCmt(e)
		})
	})
}

// paging review
pagingReview()
function pagingReview() {
	var viewCmt = document.querySelector("#viewCmt")
	var pageNumber = 1;
	var productId = document.querySelector(".product-describe")
	if (viewCmt) {
		viewCmt.addEventListener("click", () => {
			viewCmt.innerHTML = "Đang tải..."

			$.ajax({
				url: "/product/review",
				type: "get",
				data: {
					pageNumber: pageNumber,
					productId: productId.id
				},
				success: function(response) {
					viewCmt.innerHTML = `Xem thêm bình luận <i class="fa-solid fa-angle-down"></i>`
					var arrBody = response.split("|")
					pageNumber++;
					reviewCmt.innerHTML += arrBody[0]
					showStarCmt()
					reviewFeedBackCmt()
					if (arrBody[1] == pageNumber) {
						viewCmt.remove()
					}
				}
			})
		})
	}
}
