var btnMenuMini = document.querySelectorAll(".menu-mini")

btnMenuMini.forEach((btn, i) => {
	btn.addEventListener("click", () => {
		var ariaExpanded = btnMenuMini[i].getAttribute("aria-expanded")
		var right = btnMenuMini[i].querySelector(".right")
		if(ariaExpanded == 'true'){
			right.style.transform = 'rotate(90deg)'
		} else {
			right.style.transform = 'rotate(360deg)'
		}
	})
})

var instruction = document.querySelector(".instruction")
var ins = instruction.querySelector("span")
if(ins){
	instruction.innerHTML = `<a href="/admin">Quản trị viên</a> ${ins.id}`
}