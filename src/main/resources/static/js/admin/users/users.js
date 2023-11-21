// cập nhật tài khoản
function editAccount() {
	var saveData = ''
	var index = 0

	var edit = document.querySelectorAll(".edit")
	edit.forEach((event, i) => {
		var flagSave = true
		event.addEventListener("click", () => {
			index = i
			var data = edit[i].parentNode.parentNode
			saveData = data.querySelector(".data-save").innerHTML

			// gửi đi
			if (flagSave) {
				flagSave = updateAccount()
			}
		})
	})

	function updateAccount() {
		var btnSave = document.querySelectorAll(".btnSave")[index]
		btnSave.addEventListener("click", (e) => {
			if (validatedForm() == true) {
				btnSave.innerHTML = 'Đang lưu...'
				var formData = new FormData()
				var username = document.querySelectorAll(".inpUsername")[index]
				var password = document.querySelectorAll(".inpPassword")[index]
				var fullName = document.querySelectorAll(".inpFullName")[index]
				var address = document.querySelectorAll(".inpAddress")[index]
				var email = document.querySelectorAll(".inpEmail")[index]
				var phone = document.querySelectorAll(".inpPhone")[index]
				var activated = document.querySelectorAll(".inpActivated")[index]
				var role = document.querySelectorAll(".inpRole")[index]

				formData.set("username", username.value);
				formData.set("password", password.value);
				formData.set("fullName", fullName.value);
				formData.set("address", address.value);
				formData.set("email", email.value);
				formData.set("phone", phone.value);
				formData.set("activated", activated.value);
				formData.set("role", role.value);

				$.ajax({
					url: "/admin/update-account",
					type: "post",
					processData: false,  // Không xử lý dữ liệu
					contentType: false,  // Không cần ghi đè contentType
					data: formData,
					success: function(response) {
						if (response == 'success') {
							var username = document.querySelectorAll(".inpUsername")[index]
							sessionStorage.setItem("saveSC", username.value)
							location.reload()
						} else if (response == 'notUsername') {
							btnSave.innerHTML = 'Lưu thay đổi'
							var username = document.querySelectorAll(".inpUsername")[index]
							var msUsername = document.querySelectorAll(".msUsername")[index]
							msUsername.innerText = `Tên đăng nhập không tồn tại`
							username.addEventListener("input", function(e) {
								if (e.target.value.length > 0) {
									msUsername.innerText = ""
								}
							})
						}
					}
				})
			}
		})

		return false;
	}

	function validatedForm() {
		var check = true
		var username = document.querySelectorAll(".inpUsername")[index]
		var password = document.querySelectorAll(".inpPassword")[index]
		var fullName = document.querySelectorAll(".inpFullName")[index]
		var address = document.querySelectorAll(".inpAddress")[index]
		var email = document.querySelectorAll(".inpEmail")[index]
		var phone = document.querySelectorAll(".inpPhone")[index]

		var msUsername = document.querySelectorAll(".msUsername")[index]
		if (username.value.length <= 0) {
			msUsername.innerText = `Vui lòng nhập tên đăng nhập`
			check = false
		}
		username.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msUsername.innerText = ""
			}
		})

		var msPassword = document.querySelectorAll(".msPassword")[index]
		if (password.value.length <= 0) {
			msPassword.innerText = `Vui lòng nhập mật khẩu`
			check = false
		}
		password.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msPassword.innerText = ""
			}
		})

		var msFullName = document.querySelectorAll(".msFullName")[index]
		if (fullName.value.length <= 0) {
			msFullName.innerText = `Vui lòng nhập họ và tên`
			check = false
		}
		fullName.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msFullName.innerText = ""
			}
		})

		var msAddress = document.querySelectorAll(".msAddress")[index]
		if (address.value.length <= 0) {
			msAddress.innerText = `Vui lòng nhập địa chỉ`
			check = false
		}
		address.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msAddress.innerText = ""
			}
		})

		var msEmail = document.querySelectorAll(".msEmail")[index]
		var emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
		if (email.value.length <= 0) {
			msEmail.innerText = `Vui lòng nhập email`
			check = false
		} else {
			if (!emailRegex.test(email.value)) {
				msEmail.innerText = `Vui lòng nhập đúng định dạng email`
				check = false
			}
		}
		email.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msEmail.innerText = ""
			}
		})

		var msPhone = document.querySelectorAll(".msPhone")[index]
		if (phone.value.length <= 0) {
			msPhone.innerText = `Vui lòng nhập số điện thoại`
			check = false
		}
		phone.addEventListener("input", function(e) {
			if (e.target.value.length > 0) {
				msPhone.innerText = ""
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
				var data = document.querySelectorAll(".data-save")[index]
				if (e.target.classList.contains('btnReset')
					|| e.target.classList.contains('btnCloseP')
					|| e.target.classList.contains('modal')) {
					data.innerHTML = saveData
				}
			})
		})
	}
}

// xóa tài khoản
removeAccount()
function removeAccount() {
	var remove = document.querySelectorAll(".remove")
	for (let i = 0; i < remove.length; i++) {
		remove[i].addEventListener("click", () => {
			var username = remove[i].id
			var row = remove[i].parentNode.parentNode

			swal({
				text: `Bạn chắc muốn xóa tài khoản này?`,
				buttons: ["Hủy", 'Đồng ý'],
				dangerMode: true,
			})
				.then((okay) => {
					if (okay) {
						$.ajax({
							url: "/admin/delete-account",
							type: "get",
							data: {
								username: username
							},
							success: function(response) {
								if (response == 'success') {
									swal("", "Xóa tài khoản thành công", "success")
									row.remove()
								}
								else if (response == 'error') {
									swal("", "Tài khoản không thể xóa", "error")
								}
							}
						})
					}
				})
		})
	}
}

// tìm kiếm tài khoản theo tên đăng nhập, tên khách hàng, email, số điện thoại
var vlSearch = sessionStorage.getItem("vlSearch")
if (vlSearch) {
	var tableUser = document.querySelector(".tableUser")
	var searchA = document.querySelector(".searchA")
	var paging = document.querySelector("#paging")
	searchA.value = vlSearch

	tableUser.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

	if (paging) {
		if (searchA.value == '') {
			paging.style.display = "flex"
		} else {
			paging.style.display = "none"
		}
	}

	$.ajax({
		url: "/admin/search-account",
		type: "get",
		data: {
			query: searchA.value
		},
		success: function(response) {
			sessionStorage.setItem("vlSearch", searchA.value)
			tableUser.innerHTML = response
			removeAccount()

			var saveSC = sessionStorage.getItem("saveSC") // Nếu có save account thì thông báo như dưới và ngược lại là mặc định
			if (saveSC) {
				var username = document.querySelectorAll(".accountId")
				var flagId = false
				username.forEach((username, i) => {
					if (username.innerText == saveSC) {
						swal("", `Cập nhật tài khoản thành công`, "success")
						editAccount()
						document.querySelectorAll(".edit")[i].click()
						flagId = true
					}
				})

				if (!flagId) {
					swal("", `Cập nhật tài khoản thành công`, "success")
				}
				sessionStorage.removeItem("saveSC")
			} else {
				editAccount()
			}
		}
	})
} else {
	var saveSC = sessionStorage.getItem("saveSC") // Nếu có save account thì thông báo như dưới và ngược lại là mặc định
	if (saveSC) {
		var username = document.querySelectorAll(".accountId")
		var flagId = false
		username.forEach((username, i) => {
			if (username.innerText == saveSC) {
				swal("", `Cập nhật tài khoản thành công`, "success")
				editAccount()
				document.querySelectorAll(".edit")[i].click()
				flagId = true
			}
		})

		if (!flagId) {
			swal("", `Cập nhật tài khoản thành công`, "success")
		}
		sessionStorage.removeItem("saveSC")
	} else {
		editAccount()
	}
}

searchAccount()
function searchAccount() {
	var tableUser = document.querySelector(".tableUser")
	var searchA = document.querySelector(".searchA")
	var paging = document.querySelector("#paging")

	searchA.addEventListener("input", () => {
		tableUser.innerHTML = `
			<div class="loadTab text-center">
				<div class="lds-dual-ring"></div>
			</div>
		`

		if (paging) {
			if (searchA.value == '') {
				paging.style.display = "flex"
			} else {
				paging.style.display = "none"
			}
		}

		setTimeout(() => {
			$.ajax({
				url: "/admin/search-account",
				type: "get",
				data: {
					query: searchA.value
				},
				success: function(response) {
					sessionStorage.setItem("vlSearch", searchA.value)
					tableUser.innerHTML = response
					editAccount()
					removeAccount()
				}
			})
		}, 500)
	})
}