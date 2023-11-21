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

// hiện và tắt chế độ xem mô tả 
showAndHiddenDescribe()
function showAndHiddenDescribe() {
	var viewDescribe = document.querySelectorAll(".viewDescribe")
	for (let i = 0; i < viewDescribe.length; i++) {
		viewDescribe[i].addEventListener("click", () => {
			showHiddenDescribe("show", i)
		})
	}

	var closeDescribe = document.querySelectorAll(".hidden-describe")
	for (let i = 0; i < closeDescribe.length; i++) {
		closeDescribe[i].addEventListener("click", () => {
			showHiddenDescribe("hidden", i)
		})
	}

	var showDescribes = document.querySelectorAll(".show-describes")
	for (let i = 0; i < showDescribes.length; i++) {
		showDescribes[i].addEventListener("click", (e) => {
			if (e.target.classList.contains('row')) {
				showHiddenDescribe("hidden", i)
			}
		})
	}

	var showDescribe = document.querySelectorAll(".show-describe")
	for (let i = 0; i < showDescribe.length; i++) {
		showDescribe[i].addEventListener("click", () => {
			showHiddenDescribe("hidden", i)
		})
	}

	function showHiddenDescribe(action, i) {
		var showDescribe = document.querySelectorAll(".show-describe")
		var showDescribes = document.querySelectorAll(".show-describes")

		if (action == 'show') {
			showDescribe[i].style.display = 'block'
			showDescribes[i].style.display = 'block'
			document.body.style.overflow = 'hidden'
		} else {
			showDescribe[i].style.display = 'none'
			showDescribes[i].style.display = 'none'
			document.body.style.overflow = 'auto'
		}
	}
}

// cập nhật sản phẩm
function editProduct() {
	var dataProduct = ''
	var index = 0
	var arrPoster = []

	var edit = document.querySelectorAll(".edit")
	edit.forEach((event, i) => {
		var flagUploadPoster = true
		var flagUploadImage = true
		var flagAddAndRmD = true
		var flagPromotion = true
		var flagSave = true
		event.addEventListener("click", () => {
			index = i
			var product = edit[i].parentNode.parentNode
			dataProduct = product.querySelector(".data-product").innerHTML

			// default
			var showFile = product.querySelectorAll(".show-file")
			showFile.forEach((event, i) => {
				var imgsP = showFile[i].querySelector(".imgsP")
				var indexImage = String(imgsP.src).indexOf("images/") + 'images/'.length
				var nameImg = String(imgsP.src).substring(indexImage, String(imgsP.src).length)
				arrImageOld.push({
					id: event.id,
					name: nameImg
				})
			})
			var poster = product.querySelector(".poster")
			var indexPoster = String(poster.src).indexOf("images/") + 'images/'.length
			var namePoster = String(poster.src).substring(indexPoster, String(poster.src).length)
			arrPoster.push(namePoster)

			// upload ảnh đại diện
			if (flagUploadPoster) {
				flagUploadPoster = uploadPoster()
			}

			// upload nhiều mẫu ảnh tương tự
			if (flagUploadImage) {
				flagUploadImage = uploadImages()
			}

			// thêm, xóa mô tả
			if (flagAddAndRmD) {
				flagAddAndRmD = addAndRemoveDescribe()
			}

			// giá khuyến mãi
			if (flagPromotion) {
				flagPromotion = showInputPromotion()
			}

			// gửi đi
			if (flagSave) {
				flagSave = updateProduct()
			}
		})
	})

	// upload ảnh đại diện
	function uploadPoster() {
		var fileImage = document.querySelectorAll(".file")[index]
		var ctnPoster = document.querySelectorAll(".ctnPoster")[index]
		var poster = document.querySelectorAll(".poster")[index]
		var removePoster = document.querySelectorAll(".removePoster")[index]
		var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i
		var maxSize = 1 * 1024 * 1024; // 1 MB

		poster.addEventListener("click", () => {
			fileImage.click()
		})

		fileImage.addEventListener("change", function() {
			if (fileImage.files && fileImage.files[0]) {
				if (fileImage.files[0].size <= maxSize && checkImage.test(fileImage.files[0].name)) {
					removePoster.style.display = "block"
					poster.src = URL.createObjectURL(fileImage.files[0]);
					arrPoster = []
					arrPoster.push(fileImage.files[0])
				} else {
					fileImage.value = ""
					ctnPoster.innerHTML = `
					<i class="fa-solid fa-x removePoster"></i>
					<img class="poster" width="100%" height="100%">
				`
					uploadPoster()
					message('Vui lòng chọn một hình ảnh có định dạng hợp lệ và kích thước không quá 1 MB.', 'warming');
				}
			}
		})

		removePoster.addEventListener("click", () => {
			fileImage.value = ""
			arrPoster = []
			ctnPoster.innerHTML = `
				<i class="fa-solid fa-x removePoster" style="display: none;"></i>
				<img class="poster" width="100%" height="100%">
			`
			uploadPoster()
		})

		return false;
	}

	// upload nhiều mẫu ảnh tương tự
	var arrImage = []
	var arrImageOld = []
	function uploadImages() {
		var fileImages = document.querySelectorAll(".files")[index]
		var iconImage = document.querySelectorAll(".iconImage")[index]
		var showImages = document.querySelectorAll(".show-imagesP")[index]
		var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
		var maxSize = 1 * 1024 * 1024; // 2 MB
		var flagFile = true

		iconImage.addEventListener("click", () => {
			fileImages.click()
		})

		fileImages.addEventListener("change", function() {
			if (fileImages.files && fileImages.files[0]) {
				for (let i = 0; i < fileImages.files.length; i++) {
					if (fileImages.files[i].size <= maxSize && checkImage.test(fileImages.files[i].name)) {
						var showFile = document.createElement("div");
						showFile.setAttribute("class", 'show-file')
						showFile.setAttribute("id", arrImage.length + 1)
						showFile.innerHTML = `
						<button type="button" class="btnRmImage btn-close" aria-label="Close"></button>
						<img width="100%">
					`
						showImages.appendChild(showFile);
						showFile.querySelector("img").src = URL.createObjectURL(fileImages.files[i]);
						arrImage.push({
							id: showFile.id,
							files: fileImages.files[i]
						})
						flagFile = true
					} else {
						flagFile = false;
					}
				}
				fileImages.value = ''
				removeImages()
				if (!flagFile) {
					message('Vui lòng chọn một hình ảnh có định dạng hợp lệ và kích thước không quá 1 MB.', "warming");
				}
			}
		})

		// xóa ảnh
		removeImages()
		function removeImages() {
			var btnClose = document.querySelectorAll(".btnRmImage")
			if (btnClose) {
				btnClose.forEach((event) => {
					event.addEventListener("click", (e) => {
						var showFile = e.target.parentNode
						showFile.remove()

						for (let i = 0; i < arrImageOld.length; i++) {
							if (arrImageOld[i].id == showFile.id) {
								arrImageOld.splice(i, 1)
								break;
							}
							console.log(arrImageOld[i].files+"----")
						}

						for (let i = 0; i < arrImage.length; i++) {
							if (arrImage[i].id == showFile.id) {
								arrImage.splice(i, 1)
								break;
							}
							console.log(arrImage[i].files+"----")
						}

						fileImages.value = ''
					})
				})
			}
		}
		return false;
	}

	// thêm mô tả sản phẩm
	function addAndRemoveDescribe() {
		var btnAddDescribe = document.querySelectorAll(".btnAddDescribe")[index]
		var ctnDescribe = document.querySelectorAll(".ctnDescribe")[index]

		btnAddDescribe.addEventListener("click", () => {
			var drId = document.createElement("input")
			drId.setAttribute("class", "describeId form-control mt-1")
			drId.setAttribute("type", "text")
			drId.setAttribute("name", "describeId")
			drId.setAttribute("placeholder", "Loại mô tả")

			var dr = document.createElement("input")
			dr.setAttribute("class", "describe form-control mt-1")
			dr.setAttribute("type", "text")
			dr.setAttribute("name", "describe")
			dr.setAttribute("placeholder", "Mô tả sản phẩm")
			dr.style = "margin-left: 4px"

			var btnRD = document.createElement("div")
			btnRD.setAttribute("class", "btnRemoveDescribe")
			btnRD.innerText = "Xóa"

			var addDescribe = document.createElement("div")
			addDescribe.setAttribute("class", "addDescribe")

			addDescribe.appendChild(drId)
			addDescribe.appendChild(dr)
			addDescribe.appendChild(btnRD)
			ctnDescribe.appendChild(addDescribe)
			removeDescribe()
		})

		removeDescribe()
		function removeDescribe() {
			var btnRemove = document.querySelectorAll(".btnRemoveDescribe")
			if (btnRemove) {
				btnRemove.forEach((btn, i) => {
					btn.addEventListener("click", () => {
						var addDescribe = btnRemove[i].parentNode
						addDescribe.remove()
					})
				})
			}
		}
	}

	// show input khuyến mãi nếu có khuyến mãi
	function showInputPromotion() {
		var promotion = document.querySelectorAll(".promotionP")[index]
		var price = document.querySelectorAll(".inpPrice")[index]
		var discount = document.querySelectorAll(".inpDiscount")[index]
		var dcPrice = document.querySelectorAll(".dcPrice")[index]

		if (promotion.value == 'false') {
			discount.style.display = "none"
			dcPrice.style.display = "none"
			discount.value = ""
			dcPrice.value = ""
			document.querySelectorAll(".msDiscount")[index].innerHTML = ''
		}

		promotion.addEventListener("change", (e) => {
			if (e.target.value == 'true') {
				discount.style.display = "inline"
			} else {
				discount.style.display = "none"
				dcPrice.style.display = "none"
				discount.value = ""
				dcPrice.value = ""
				document.querySelectorAll(".msDiscount")[index].innerHTML = ''
			}
		})

		discount.addEventListener("input", (e) => {
			var value = e.target.value;
			if (value.length > 0 && price.value.length > 0) {
				dcPrice.style.display = "inline"
				var vlDcPrice = Number(price.value * ((100 - value) / 100)).toLocaleString("vi-VN")
				dcPrice.innerHTML = vlDcPrice + ' đ'
			}

			if (value.length == 0) {
				dcPrice.style.display = "none"
			}
		})

		price.addEventListener("input", (e) => {
			var value = e.target.value;
			if (value.length > 0 && discount.value.length > 0) {
				dcPrice.style.display = "inline"
				var vlDcPrice = Number(value * ((100 - discount.value) / 100)).toLocaleString("vi-VN")
				dcPrice.innerHTML = vlDcPrice + ' đ'
			}

			if (value.length == 0) {
				dcPrice.style.display = "none"
			}
		})

		return false;
	}

	// gửi đi
	function updateProduct() {
		var btnSave = document.querySelectorAll(".btnSave")[index]
		btnSave.addEventListener("click", (e) => {
			if (validatedForm() == true) {
				btnSave.innerHTML = 'Đang lưu...'
				var formData = new FormData()
				var productId = e.target.id
				var name = document.querySelectorAll(".inpName")[index]
				var poster = document.querySelectorAll(".file")[index]
				var price = document.querySelectorAll(".inpPrice")[index]
				var quantity = document.querySelectorAll(".inpQuantity")[index]
				var arrDescribe = new Array()
				var ctnDescribe = document.querySelectorAll(".ctnDescribe")[index]
				var describeId = ctnDescribe.querySelectorAll(".describeId")
				var describe = ctnDescribe.querySelectorAll(".describe")
				var promotion = document.querySelectorAll(".promotionP")[index]
				var discount = document.querySelectorAll(".inpDiscount")[index]
				var category = document.querySelectorAll(".category")[index]

				formData.set("productId", productId)
				formData.set("name", name.value)
				formData.set("poster", poster.files[0])

				for (let i = 0; i < arrImage.length; i++) {
					formData.append("images", arrImage[i].files)
				}
				for (let i = 0; i < arrImageOld.length; i++) {
					formData.append("nameImagesOld", arrImageOld[i].name)
				}

				formData.set("price", price.value)
				formData.set("quantity", quantity.value)

				for (let i = 0; i < describeId.length; i++) {
					arrDescribe.push(`${describeId[i].value}|${describe[i].value}`)
				}
				formData.set("describe", arrDescribe)

				formData.set("promotion", promotion.value)
				if (promotion.value == 'true') {
					formData.set("discount", discount.value)
				}

				formData.set("category", category.value)

				$.ajax({
					url: "/admin/update-product",
					type: "post",
					processData: false,  // Không xử lý dữ liệu
					contentType: false,  // Không cần ghi đè contentType
					data: formData,
					success: function(response) {
						if (response == 'success') {
							sessionStorage.setItem("saveSC", productId)
							location.reload()
						}
					}
				})
			}
		})

		return false;
	}

	// xác thực form
	function validatedForm() {
		var check = true;
		var msName = document.querySelectorAll(".msName")[index]
		var msImage = document.querySelectorAll(".msImage")[index]
		var msPrice = document.querySelectorAll(".msPrice")[index]
		var msQuantity = document.querySelectorAll(".msQuantity")[index]
		var msDescribe = document.querySelectorAll(".msDescribe")[index]
		var msDiscount = document.querySelectorAll(".msDiscount")[index]
		var msCategory = document.querySelectorAll(".msCategory")[index]

		var name = document.querySelectorAll(".inpName")[index]
		if (name.value.length <= 0) {
			msName.innerText = `Vui lòng nhập tên sản phẩm`
			check = false
		}
		name.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msName.innerText = ""
			}
		})

		var fileImage = document.querySelectorAll(".file")[index]
		if (arrPoster.length <= 0) {
			msImage.innerText = 'Vui lòng chọn ảnh đại diện'
			check = false;
		}
		fileImage.addEventListener("change", () => {
			arrPoster = []
			arrPoster.push(fileImage.files[0])
			msImage.innerText = ''
		})

		var price = document.querySelectorAll(".inpPrice")[index]
		if (price.value.length <= 0) {
			msPrice.innerText = `Vui lòng nhập giá sản phẩm`
			check = false
		} else {
			if (price.value < 0) {
				msPrice.innerText = `Vui lòng nhập giá phải lớn hơn 0`
				check = false
			}
		}
		price.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msPrice.innerText = ""
			}
		})

		var quantity = document.querySelectorAll(".inpQuantity")[index]
		if (quantity.value.length <= 0) {
			msQuantity.innerText = `Vui lòng nhập số lượng sản phẩm`
			check = false
		} else {
			if (quantity.value < 0) {
				msQuantity.innerText = `Vui lòng nhập số lượng phải lớn hơn 0`
				check = false
			}
		}
		quantity.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msQuantity.innerText = ""
			}
		})

		var ctnDescribe = document.querySelectorAll(".ctnDescribe")[index]
		var describeId = ctnDescribe.querySelectorAll(".describeId")
		describeId.forEach((e) => {
			if (e.value.length <= 0) {
				msDescribe.innerText = `Vui lòng nhập đầy đủ loại và mô tả sản phẩm`
				check = false
			}
		})
		describeId.forEach((event) => {
			event.addEventListener("input", function(e) {
				if (e.target.value.length > 0) {
					msDescribe.innerText = ''
				}
			})
		})

		var describe = ctnDescribe.querySelectorAll(".describe")
		describe.forEach((e) => {
			if (e.value.length <= 0) {
				msDescribe.innerText = `Vui lòng nhập đầy đủ loại và mô tả sản phẩm`
				check = false
			}
		})
		describe.forEach((event) => {
			event.addEventListener("input", function checkValue(e) {
				if (e.target.value.length > 0) {
					msDescribe.innerText = ''
					event.removeEventListener("input", checkValue)
				}
			})
		})

		var promotion = document.querySelectorAll(".promotionP")[index]
		var discount = document.querySelectorAll(".inpDiscount")[index]
		if (promotion.value == 'true') {
			if (discount.value.length <= 0) {
				msDiscount.innerText = `Vui lòng nhập phần trăm khuyến mãi`
				check = false
			} else {
				if (discount.value < 0) {
					msDiscount.innerText = `Vui lòng nhập phần trăm phải lớn hơn 0`
					check = false
				}
			}
			discount.addEventListener("input", function checkValue(e) {
				if (e.target.value.length > 0) {
					msDiscount.innerText = ""
					discount.removeEventListener("input", checkValue)
				}
			})
		}

		var category = document.querySelectorAll(".category")[index]
		if (category.value == '0') {
			msCategory.innerText = `Vui lòng chọn loại sản phẩm`
			check = false
		}
		category.addEventListener("change", function checkValue(e) {
			if (e.target.value != '0') {
				msCategory.innerText = ''
				category.removeEventListener("change", checkValue)
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
				var data = document.querySelectorAll(".data-product")[index]
				if (e.target.classList.contains('btnReset')
					|| e.target.classList.contains('btnCloseP')
					|| e.target.classList.contains('modal')) {
					data.innerHTML = dataProduct

					var fileImage = document.querySelectorAll(".file");
					var fileImages = document.querySelectorAll(".files")
					arrImage = []
					arrImageOld = []
					fileImage[index].value = ''
					fileImages[index].value = ''
					uploadPoster()
					uploadImages()
					addAndRemoveDescribe()
					showInputPromotion()
				}
			})
		})
	}
}

// xóa sản phẩm
removeProduct()
function removeProduct() {
	var remove = document.querySelectorAll(".remove")
	for (let i = 0; i < remove.length; i++) {
		remove[i].addEventListener("click", () => {
			var id = remove[i].id
			var row = remove[i].parentNode.parentNode

			swal({
				text: `Bạn chắc muốn xóa sản phẩm này?`,
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/admin/delete-product",
							type: "get",
							data: {
								productId: id
							},
							success: function(response) {
								if (response == 'success') {
									swal("", "Xóa sản phẩm thành công", "success")
									row.remove()
								}
								else if (response == 'error') {
									swal("", "Sản phẩm này không thể xóa", "error")
								}
							}
						})
					}
				})
		})
	}
}

// tìm kiếm theo id hoặc tên và lọc sản phẩm giá khuyến mãi và số lượng
var vlSearch = sessionStorage.getItem("vlSearch")
if (vlSearch) {
	var tableP = document.querySelector(".tableP")
	var searchP = document.querySelector(".searchP")
	var paging = document.querySelector("#paging")
	var dataFilter = document.querySelector(".btnFilter").getAttribute("data-filter")
	searchP.value = vlSearch

	if (paging) {
		if (searchP.value == '') {
			paging.style.display = "flex"
		} else {
			paging.style.display = "none"
		}
	}

	tableP.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

	$.ajax({
		url: "/admin/search-product",
		type: "get",
		data: {
			query: searchP.value,
			dataFilter: dataFilter
		},
		success: function(response) {
			sessionStorage.setItem("vlSearch", searchP.value)
			tableP.innerHTML = response
			showAndHiddenImages()
			showAndHiddenDescribe()
			removeProduct()

			var saveSC = sessionStorage.getItem("saveSC") // Nếu có save product thì thông báo như dưới và ngược lại là mặc định
			if (saveSC) {
				var productId = document.querySelectorAll(".productId")
				var flagId = false
				productId.forEach((id, i) => {
					if (id.innerHTML == saveSC) {
						swal("", `Cập nhật sản phẩm thành công`, "success")
						editProduct()
						document.querySelectorAll(".edit")[i].click()
						flagId = true
					}
				})

				if (!flagId) {
					swal("", `Cập nhật sản phẩm thành công`, "success")
				}
				sessionStorage.removeItem("saveSC")
			} else {
				editProduct()
			}
		}
	})
} else {
	var saveSC = sessionStorage.getItem("saveSC") // Nếu có save product thì thông báo như dưới và ngược lại là mặc định
	if (saveSC) {
		var productId = document.querySelectorAll(".productId")
		var flagId = false
		productId.forEach((id, i) => {
			if (id.innerText == saveSC) {
				swal("", `Cập nhật sản phẩm thành công`, "success")
				editProduct()
				document.querySelectorAll(".edit")[i].click()
				flagId = true
			}
		})

		if (!flagId) {
			swal("", `Cập nhật sản phẩm thành công`, "success")
		}
		sessionStorage.removeItem("saveSC")
	} else {
		editProduct()
	}
}

searchAndFilterProduct()
function searchAndFilterProduct() {
	var tableP = document.querySelector(".tableP")
	var searchP = document.querySelector(".searchP")
	var paging = document.querySelector("#paging")
	var dataFilter = document.querySelector(".btnFilter").getAttribute("data-filter")

	searchP.addEventListener("input", () => {
		tableP.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

		if (paging) {
			if (searchP.value == '') {
				paging.style.display = "flex"
			} else {
				paging.style.display = "none"
			}
		}

		setTimeout(() => {
			$.ajax({
				url: "/admin/search-product",
				type: "get",
				data: {
					query: searchP.value,
					dataFilter: dataFilter
				},
				success: function(response) {
					sessionStorage.setItem("vlSearch", searchP.value)
					tableP.innerHTML = response
					showAndHiddenImages()
					showAndHiddenDescribe()
					editProduct()
					removeProduct()
				}
			})
		}, 500)
	})
}