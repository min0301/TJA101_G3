(function () {
    const konamiCode = [
        "ArrowUp", "ArrowUp",
        "ArrowDown", "ArrowDown",
        "ArrowLeft", "ArrowRight",
        "ArrowLeft", "ArrowRight",
        "KeyB", "KeyA"
    ];

    let inputSequence = [];

    window.addEventListener("keydown", function (event) {
        inputSequence.push(event.code);

        if (inputSequence.length > konamiCode.length) {
            inputSequence.shift();
        }

        if (inputSequence.join() === konamiCode.join()) {
            // 顯示提示動畫
            const overlay = document.getElementById("konami-overlay");
            overlay.classList.add("show");

            // 幾秒後跳轉
            setTimeout(() => {
                window.location.href = "/back-end/adm/AdmLogin.html";
            }, 2500); // 2.5秒後跳轉
        }
    });
})();



