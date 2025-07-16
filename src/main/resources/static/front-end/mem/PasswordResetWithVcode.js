document.getElementById('resetForm').addEventListener('submit', function(e) {
           e.preventDefault();
           const email = localStorage.getItem('emailForReset');
           const vcode = document.getElementById('Vcode').value.trim();
           const password = document.getElementById('password').value.trim();
           const passwordConfirm = document.getElementById('passwordConfirm').value.trim();
           const result = document.getElementById('result');

           if (!email) {
               result.textContent = '錯誤：找不到信箱資訊，請從忘記密碼流程重新操作。';
               return;
           }
           if (!vcode || !password || !passwordConfirm) {
               result.textContent = '請填寫所有欄位';
               return;
           }
           if (password !== passwordConfirm) {
               result.textContent = '兩次輸入的新密碼不一致';
               return;
           }

           fetch('/api/members/reset-passwordV', {
               method: 'POST',
               headers: {'Content-Type': 'application/json'},
               body: JSON.stringify({ email, Vcode: vcode, password, passwordConfirm })
           })
           .then(res => res.json())
           .then(data => {
               if (data.success) {
                   result.style.color = '#25945a';
                   result.textContent = data.message;
                   setTimeout(() => window.location.href = '/index.html', 1200);
               } else {
                   result.style.color = '#b94040';
                   result.textContent = data.message;
               }
           })
           .catch(() => {
               result.style.color = '#b94040';
               result.textContent = '系統錯誤，請稍後再試';
           });
       });