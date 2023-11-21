// upload ảnh đại diện
var fileImage = document.querySelector(".file");
uploadPoster()
function uploadPoster() {
	var ctnPoster = document.querySelector(".ctnPoster");
	var poster = document.querySelector(".poster");
	var removePoster = document.querySelector(".removePoster");
	var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
	var maxSize = 1 * 1024 * 1024; // 1 MB

	poster.addEventListener("click", () => {
		fileImage.click()
	})

	fileImage.addEventListener("change", function() {
		if (fileImage.files && fileImage.files[0]) {
			if (fileImage.files[0].size <= maxSize && checkImage.test(fileImage.files[0].name)) {
				removePoster.style.display = "block"
				poster.src = URL.createObjectURL(fileImage.files[0]);
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
		ctnPoster.innerHTML = `
			<i class="fa-solid fa-x removePoster"></i>
			<img class="poster" width="100%" height="100%">
		`
		uploadPoster()
	})
}

// upload nhiều mẫu ảnh tương tự
var arrImage = []
var fileImages = document.querySelector(".files")
uploadImages()
function uploadImages() {
	var iconImage = document.querySelectorAll(".iconImage")
	var showImages = document.querySelector(".show-images")
	var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
	var maxSize = 1 * 1024 * 1024; // 2 MB
	var flagFile = true

	iconImage.forEach((event) => {
		event.addEventListener("click", () => {
			fileImages.click()
		})
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

			if (!flagFile) {
				message('Vui lòng chọn một hình ảnh có định dạng hợp lệ và kích thước không quá 1 MB.', "warming");
			}
		}

		var btnClose = document.querySelectorAll(".btn-close")
		btnClose.forEach((event) => {
			event.addEventListener("click", (e) => {
				var showFile = e.target.parentNode
				showFile.remove()

				for (let i = 0; i < arrImage.length; i++) {
					if (arrImage[i].id == showFile.id) {
						arrImage.splice(i, 1)
						break;
					}
				}

				fileImages.value = ''
			})
		})
	})
}

// thêm mô tả sản phẩm
addAndRemoveDescribe()
function addAndRemoveDescribe() {
	var btnAddDescribe = document.querySelector(".btnAddDescribe")
	var ctnDescribe = document.querySelector(".ctnDescribe")

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

		var describeId = document.querySelectorAll(".describeId")
		var describe = document.querySelectorAll(".describe")
		var btnRemove = document.querySelectorAll(".btnRemoveDescribe")
		btnRemove.forEach((btn, i) => {
			btn.addEventListener("click", e => {
				e.target.remove()
				describeId[i + 1].remove()
				describe[i + 1].remove()
			})
		})
	})
}

// show input khuyến mãi nếu có khuyến mãi
showInputPromotion()
function showInputPromotion() {
	var promotion = document.querySelector(".promotion")
	var price = document.querySelector("#price")
	var discount = document.querySelector("#discount")
	var dcPrice = document.querySelector(".dcPrice")

	if (promotion.value == 'false') {
		discount.style.display = "none"
		dcPrice.style.display = "none"
		discount.value = ""
		dcPrice.value = ""
		document.querySelector("#msDiscount").innerHTML = ''
	}

	promotion.addEventListener("change", (e) => {
		if (e.target.value == 'true') {
			discount.style.display = "inline"
		} else {
			discount.style.display = "none"
			dcPrice.style.display = "none"
			discount.value = ""
			dcPrice.value = ""
			document.querySelector("#msDiscount").innerHTML = ''
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
}

// reset form
var btnReset = document.querySelector(".btnReset")
btnReset.addEventListener("click", resetForm)

function resetForm() {
	var inp = document.querySelectorAll(".inp")
	var message = document.querySelectorAll(".message")
	var ctnPoster = document.querySelector(".ctnPoster")
	var showImages = document.querySelector(".show-images")
	var promotion = document.querySelector(".promotion")
	var dfPromotion = promotion.value
	var discount = document.querySelector("#discount")
	var dcPrice = document.querySelector(".dcPrice")
	var category = document.querySelector(".category")
	var dfCategory = category.value

	message.forEach((e) => {
		e.innerHTML = ""
	})

	inp.forEach((e) => {
		e.value = ""
	})

	fileImage.value = ''
	ctnPoster.innerHTML = `
			<i class="fa-solid fa-x removePoster"></i>
			<img class="poster" width="100%" height="100%">
		`
	uploadPoster()

	fileImages.value = ''
	showImages.innerHTML = ''
	arrImage = []

	promotion.value = dfPromotion
	discount.style.display = "none"
	dcPrice.style.display = "none"
	discount.value = ""
	dcPrice.value = ""

	category.value = dfCategory

	var describeId = document.querySelectorAll(".describeId")
	var describe = document.querySelectorAll(".describe")
	var btnRemove = document.querySelectorAll(".btnRemoveDescribe")
	describeId[0].value = ''
	describe[0].value = ''
	btnRemove.forEach((btn, i) => {
		btn.remove()
		describeId[i + 1].remove()
		describe[i + 1].remove()
	})
}

// validated form addProduct
function validatedForm() {
	var check = true;
	var msName = document.querySelector("#msName")
	var msImage = document.querySelector("#msImage")
	var msPrice = document.querySelector("#msPrice")
	var msQuantity = document.querySelector("#msQuantity")
	var msDescribe = document.querySelector("#msDescribe")
	var msDiscount = document.querySelector("#msDiscount")
	var msCategory = document.querySelector("#msCategory")

	var name = document.querySelector("#name")
	if (name.value.length <= 0) {
		msName.innerText = `Vui lòng nhập tên sản phẩm`
		check = false
	}
	name.addEventListener("input", function(e) {
		if (e.target.value.length > 0) {
			msName.innerText = ""
		}
	})

	if (fileImage.value == '') {
		msImage.innerText = 'Vui lòng chọn ảnh đại diện'
		check = false
	}
	fileImage.addEventListener("change", () => {
		msImage.innerText = ''
	})

	var price = document.querySelector("#price")
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

	var quantity = document.querySelector("#quantity")
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

	var describeId = document.querySelectorAll(".describeId")
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

	var describe = document.querySelectorAll(".describe")
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

	var promotion = document.querySelector(".promotion")
	var discount = document.querySelector("#discount")
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

	var category = document.querySelector(".category")
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

// thêm sản phẩm
addProduct()
function addProduct() {
	var btnSave = document.querySelector(".btnSave")
	btnSave.addEventListener("click", () => {
		var formData = new FormData();
		var name = document.querySelector("#name")
		var poster = document.querySelector(".file")
		var price = document.querySelector("#price")
		var quantity = document.querySelector("#quantity")
		var arrDescribe = new Array()
		var describeId = document.querySelectorAll(".describeId")
		var describe = document.querySelectorAll(".describe")
		var promotion = document.querySelector(".promotion")
		var discount = document.querySelector("#discount")
		var category = document.querySelector(".category")

		if (validatedForm()) {
			formData.set("name", name.value)
			formData.set("poster", poster.files[0])

			for (let i = 0; i < arrImage.length; i++) {
				formData.append("images", arrImage[i].files)
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
				url: "/admin/products/addProduct",
				type: "post",
				processData: false,  // Không xử lý dữ liệu
				contentType: false,  // Không cần ghi đè contentType
				data: formData,
				success: function(response) {
					if (response == 'success') {
						swal("", "Thêm sản phẩm thành công", "success")
						resetForm()
					}
				}
			})
		}
	})
}
