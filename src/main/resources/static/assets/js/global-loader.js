document.addEventListener("DOMContentLoaded", function () {

    // Header 邏輯保持不變，因為它目前是正常的
    const headerPlaceholder = document.getElementById('header-placeholder');
    if (headerPlaceholder) {
        fetch('/_header.html')
            .then(response => {
                if (!response.ok) throw new Error(`載入 Header 失敗: ${response.statusText}`);
                return response.text();
            })
            .then(data => {
                headerPlaceholder.innerHTML = data;
                // 為 Header 也加上，確保一致性
                setTimeout(function () {
                    const aosElements = headerPlaceholder.querySelectorAll('[data-aos]');
                    aosElements.forEach(el => el.classList.add('aos-animate'));
                }, 100);
            })
            .catch(error => {
                console.error('載入 Header 時發生錯誤:', error);
                headerPlaceholder.innerHTML = '<p style="color:red; text-align:center;">Header 載入失敗。</p>';
            });
    }

    const footerPlaceholder = document.getElementById('footer-placeholder');
    if (footerPlaceholder) {
        fetch('/_footer.html')
            .then(response => {
                if (!response.ok) throw new Error(`載入 Footer 失敗: ${response.statusText}`);
                return response.text();
            })
            .then(data => {
                footerPlaceholder.innerHTML = data;

                // 【【【 最終解決方案：手動觸發 AOS 動畫 】】】
                // 我們給予一個稍微長一點的延遲 (100毫秒)，確保瀏覽器有充足時間完成渲染
                setTimeout(function () {
                    // 1. 只尋找 footer placeholder 內部帶有 data-aos 的所有元素
                    const aosElementsInFooter = footerPlaceholder.querySelectorAll('[data-aos]');

                    if (aosElementsInFooter.length > 0) {
                        console.log(`在 Footer 中找到 ${aosElementsInFooter.length} 個 AOS 元素，現在手動觸發它們的動畫...`);

                        // 2. 遍歷這些元素，並手動為它們加上 'aos-animate' class
                        aosElementsInFooter.forEach(el => {
                            el.classList.add('aos-animate');
                        });
                    } else {
                        console.log("Footer 中沒有找到需要 AOS 動畫的元素。");
                    }
                }, 100); // 使用 100ms 延遲，確保萬無一失
            })
            .catch(error => {
                console.error('載入 Footer 時發生錯誤:', error);
                footerPlaceholder.innerHTML = '<p style="color:red; text-align:center;">Footer 載入失敗。</p>';
            });
    }
});