var inputs = document.querySelectorAll(".form-control")
inputs.forEach((event) => {
	event.addEventListener("input", (e) =>{
		var message = e.target.parentNode.querySelector(".message")
		if(e.target.value.length > 0){
			message.innerHTML = ''
		}
	})
})