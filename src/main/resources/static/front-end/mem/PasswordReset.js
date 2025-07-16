 document.getElementById('resetForm').addEventListener('submit', function(e) {
            e.preventDefault();

            // 取得 email（這裡假設存在 localStorage.emailForReset）
            const email = localStorage.getItem('emailForReset');
            const oldPassword = document.getElementById('oldPassword').value.trim();
            const newPassword = document.getElementById('newPassword').value.trim();
            const newPasswordConfirm = document.getElementById('newPasswordConfirm').value.trim();
            const resultMsg = document.getElementById('resultMsg');

            // 基本檢查
            if (!email) {
                resultMsg.textContent = '錯誤：找不到信箱資訊，請從忘記密碼流程重新操作。';
                return;
            }
            if (!oldPassword || !newPassword || !newPasswordConfirm) {
                resultMsg.textContent = '請填寫所有欄位';
                return;
            }
            if (newPassword !== newPasswordConfirm) {
                resultMsg.textContent = '兩次輸入的新密碼不一致';
                return;
            }

            // 呼叫 API
            fetch('/api/members/reset-password', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    email: email,
                    oldPassword: oldPassword,
                    newPassword: newPassword,
                    newPasswordConfirm: newPasswordConfirm
                })
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    resultMsg.style.color = '#25945a';
                    resultMsg.textContent = data.message || '密碼重設成功';
                } else {
                    resultMsg.style.color = '#b94040';
                    resultMsg.textContent = data.message || '密碼重設失敗，請檢查資料';
                }
            })
            .catch(() => {
                resultMsg.style.color = '#b94040';
                resultMsg.textContent = '系統錯誤，請稍後再試';
            });
        });