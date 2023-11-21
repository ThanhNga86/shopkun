// thêm danh mục
addCategory()
function addCategory() {
	var btnReset = document.querySelector(".btnReset")
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
				addCategory()
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
		addCategory()
	})

	// hủy
	btnReset.addEventListener("click", () => {
		resetForm()
	})
	function resetForm() {
		inp.forEach((event) => {
			event.value = ''
		})

		fileC.value = ''
		ctnImageC.innerHTML = `
			<i class="fa-solid fa-x removeImageC"></i>
			<img class="imageC" width="100%" height="100%">
		`
		addCategory()
	}
}