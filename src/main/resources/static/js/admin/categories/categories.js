// chỉnh sửa danh mục
editCategory()
function editCategory() {
	var edit = document.querySelectorAll(".edit")
	for (let i = 0; i < edit.length; i++) {
		var flagImage = true;
		var flagSave = true;
		edit[i].addEventListener("click", () => {
			var btnSave = document.querySelector(".btnSave")
			var ctnImageC = document.querySelector(".ctnImageC")
			var removeImageC = document.querySelector(".removeImageC")
			var nameC = document.querySelector("#nameC")
			var fileC = document.querySelector(".fileC")
			var imageC = document.querySelector(".imageC")
			var row = edit[i].parentNode.parentNode
			var id = edit[i].id

			// default
			imageC.src = row.querySelector("img").src
			nameC.value = row.querySelector(".name").innerText
			removeImageC.style.display = "block"
			fileC.value = ''
			document.querySelector("#msName").innerHTML = ''
			document.querySelector("#msImage").innerHTML = ''

			// xóa hình
			removeImageC.addEventListener("click", () => {
				fileC.value = ''
				ctnImageC.innerHTML = `
					<i class="fa-solid fa-x removeImageC"></i>
					<img class="imageC" width="100%" height="100%">
				`
				flagImage = updateCategory()
			})

			if (flagImage) {
				flagImage = updateCategory()
			}

			if (flagSave) {
				btnSave.addEventListener("click", () => {
					flagSave = false;
					var check = true
					var msName = document.querySelector("#msName")
					var msImage = document.querySelector("#msImage")
					var imageC = document.querySelector(".imageC")

					if (nameC.value == '') {
						msName.innerHTML = `Vui lòng nhập tên danh mục`
						check = false
					}

					if (imageC.src == '') {
						msImage.innerHTML = `Vui lòng chọn hinh ảnh`
						check = false
					}

					if (check) {
						btnSave.innerHTML = 'Đang lưu...'
						var formData = new FormData()
						formData.set("categoryId", id)
						formData.set("name", nameC.value)
						formData.set("image", fileC.files[0])

						$.ajax({
							url: "/admin/update-category",
							type: "post",
							processData: false,  // Không xử lý dữ liệu
							contentType: false,  // Không cần ghi đè contentType
							data: formData,
							success: function(response) {
								if (response == 'success') {
									btnSave.innerHTML = 'Lưu thay đổi'
									swal("", "Thay đổi thành công", "success")
									var img = row.querySelector("img")
									var name = row.querySelector(".name")
									name.innerText = nameC.value
									img.src = imageC.src
								}
							}
						})
					}
				})
			}
		})
	}

	// cập nhật danh mục
	function updateCategory() {
		var inp = document.querySelectorAll(".inp")
		var ctnImageC = document.querySelector(".ctnImageC")
		var removeImageC = document.querySelector(".removeImageC")
		var fileC = document.querySelector(".fileC")
		var imageC = document.querySelector(".imageC")

		imageC.addEventListener("click", () => {
			fileC.click()
		})

		inp.forEach((event) => {
			event.addEventListener("input", (e) => {
				var ms = e.target.parentNode.querySelector(".message")
				ms.innerHTML = ''
			})
		})

		fileC.addEventListener("change", (e) => {
			var checkImage = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
			var maxSize = 1 * 1024 * 1024; // 1 MB

			if (fileC.files && fileC.files[0]) {
				if (fileC.files[0].size <= maxSize && checkImage.test(fileC.files[0].name)) {
					removeImageC.style.display = "block"
					imageC.src = URL.createObjectURL(fileC.files[0]);

					var ms = e.target.parentNode.querySelector(".message")
					ms.innerHTML = ''
				} else {
					fileC.value = ''
					ctnImageC.innerHTML = `
						<i class="fa-solid fa-x removeImageC"></i>
						<img class="imageC" width="100%" height="100%">
					`
					updateCategory()
					message('Vui lòng chọn một hình ảnh có định dạng hợp lệ và kích thước không quá 1 MB.', 'warming');
				}
			}
		})

		// xóa hình
		removeImageC.addEventListener("click", () => {
			fileC.value = ''
			ctnImageC.innerHTML = `
			<i class="fa-solid fa-x removeImageC"></i>
			<img class="imageC" width="100%" height="100%">
		`
			updateCategory()
		})

		return false;
	}
}

// xóa danh mục
removeCategory()
function removeCategory() {
	var remove = document.querySelectorAll(".remove")
	for (let i = 0; i < remove.length; i++) {
		remove[i].addEventListener("click", () => {
			var id = remove[i].id
			var row = remove[i].parentNode.parentNode

			swal({
				text: `Bạn chắc muốn xóa danh mục này?`,
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/admin/delete-category",
							type: "get",
							data: {
								categoryId: id
							},
							success: function(response) {
								if (response == 'success') {
									swal("", "Xóa danh mục thành công", "success")
									row.remove()
								}
								else if (response == 'error') {
									swal("", "Danh mục này không thể xóa", "error")
								}
							}
						})
					}
				})
		})
	}
}

// tìm kiếm danh mục
searchCategory()
function searchCategory() {
	var tableC = document.querySelector(".tableC")
	var searchC = document.querySelector(".searchC")
	var paging = document.querySelector("#paging")

	searchC.addEventListener("input", () => {
		tableC.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`
		if (searchC.value == '') {
			if (paging) {
				paging.style.display = "flex"
			}
		} else {
			if (paging) {
				paging.style.display = "none"
			}
		}

		$.ajax({
			url: "/admin/search-category",
			type: "get",
			data: {
				query: searchC.value,
			},
			success: function(response) {
				tableC.innerHTML = response
				editCategory()
				removeCategory()
			}
		})
	})
}