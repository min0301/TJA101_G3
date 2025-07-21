(function () {
    const konamiCode = [
        "ArrowUp", "ArrowUp",
        "ArrowDown", "ArrowDown",
        "ArrowLeft", "ArrowRight",
        "KeyB", "KeyA"
    ];

    const swaggerCode = [
        "KeyL", "KeyF",
        "Digit2","Period",
        "KeyN", "KeyE",
        "KeyT"
    ];

    let inputSequence = [];

    window.addEventListener("keydown", function (event) {
        inputSequence.push(event.code);

        // 保留最近幾個輸入
        const maxLength = Math.max(konamiCode.length, swaggerCode.length);
        if (inputSequence.length > maxLength) {
            inputSequence.shift();
        }

        // Konami Code 檢查
        if (inputSequence.slice(-konamiCode.length).join() === konamiCode.join()) {
            const overlay = document.getElementById("konami-overlay");
            overlay.classList.add("show");
            setTimeout(() => {
                window.location.href = "/back-end/adm/AdmLogin.html";
            }, 1500);
        }

        // Swagger Code 檢查
        if (inputSequence.slice(-swaggerCode.length).join() === swaggerCode.join()) {
            alert("Swagger Secret Code Triggered!");
            window.location.href = "/swagger-ui/index.html";
        }
    });
})();



