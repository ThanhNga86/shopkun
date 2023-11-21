const keyframes = `
    @keyframes message {
        0% {
            opacity: 1;
        }
        20% {
            opacity: 1;
        }
        40% {
            opacity: 0.9;
        }
        50% {
            opacity: 0.75;
        }
        60% {
            opacity: 0.6;
        }
        70% {
            opacity: 0.45;
        }
        80% {
            opacity: 0.3;
        }
        90% {
            opacity: 0.15;
        }
        100% {
            opacity: 0;
        }
    }

    @keyframes appear {
        0%{
            transform: translateX(110%);
        }
        100%{
            transform: translateX(0%);
        }
    }
`;

const style = document.createElement('style');
style.innerHTML = keyframes;
document.head.appendChild(style);

var containerMS = document.createElement("div")
document.body.appendChild(containerMS);
function message(message, status) {
    containerMS.style = `position: fixed; top: 5px; right: 5px; z-index: 100000`

    var msElement = document.createElement("div");
    msElement.setAttribute("class", "message");
    containerMS.appendChild(msElement);

    var ms = document.querySelectorAll(".message")
    ms.forEach(btn => {
        btn.addEventListener("click", (e) => {
            if(!e.target.classList.contains('message') ){
                e.target.parentNode.remove()
            } else {
                e.target.remove();
            }
        })
    })

    if(status == 'success'){
        msElement.style =
        `margin: 3px; padding: 8px; border-radius: 5px; font-size: large; background-color: rgb(16,193,10); cursor: pointer;color: white;
        animation: message ease-in-out 4s forwards, appear ease-out 0.1s; z-index: 100000;`;
        var icon = '<i class="fa-regular fa-circle-check"></i>';
        msElement.innerHTML = icon + ` ${message}`
        setTimeout(() => {
            msElement.remove();
        }, 3800)
    } 
    else if(status == 'warming'){
        msElement.style =
        `margin: 3px; padding: 8px; border-radius: 5px; font-size: large; background-color: rgb(255,234,57); cursor: pointer;color: black;
        animation: message ease-in-out 4s forwards, appear ease-out 0.1s;z-index: 100000;`;
        var icon = '<i class="fa-solid fa-triangle-exclamation" style="color: black"></i>';
        msElement.innerHTML = icon + ` ${message}`
        setTimeout(() => {
            msElement.remove();
        }, 3800)
    }
}
