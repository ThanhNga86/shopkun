var message = document.querySelectorAll(".message")
var inp = document.querySelectorAll(".inp")
inp.forEach((btn) => {
	btn.addEventListener("input", e => {
		if(e.target.value.length > 0){
			message.forEach((ms) => {
				ms.innerHTML = ""
			})
		}
	})
})