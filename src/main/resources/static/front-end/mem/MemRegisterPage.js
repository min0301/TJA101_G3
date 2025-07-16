let emailIsUnique = false;

async function checkEmailDuplicate() {
    const emailInput = document.getElementById('memEmail');
    const emailMsg = document.getElementById('emailMsg');
    const email = emailInput.value.trim();
    if (!email) {
        emailMsg.textContent = '❌請填入信箱';
        emailMsg.style.color = "#ad3131"
        emailIsUnique = false;
        return;
    } else if (!/^[A-Za-z0-9._%+-]+@(?:gmail\.com|yahoo\.com(\.tw)?|hotmail\.com|outlook\.com|icloud\.com|msn\.com|hotmail\.com\.tw)$/i.test(email)){
    	emailMsg.textContent = '❌信箱格式不符';
        emailMsg.style.color = "#ad3131"
        emailIsUnique = false;
        return;
    }
    try {
        const resp = await fetch('/api/members/check-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        const result = await resp.json();
        if (result.exist) {
            emailMsg.textContent = result.message;
            emailMsg.style.color = "#ad3131"
            emailIsUnique = false;
        } else {
            emailMsg.textContent = result.message;
            emailMsg.style.color = "#31ad50";
            emailIsUnique = true;
        }
    } catch (err) {
        emailMsg.textContent = '目前無法檢查信箱，請稍後再試';
        emailMsg.style.color = "#c14444";
        emailIsUnique = false;
    }
}

document.getElementById('memRegister').addEventListener('submit', async function(e) {
    e.preventDefault();
    const emailInput = document.getElementById('memEmail');
    const emailMsg = document.getElementById('emailMsg');
    const memPassword = document.getElementById('memPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    if (memPassword !== confirmPassword) {
        alert('密碼與確認密碼不一致！');
        document.getElementById('confirmPassword').focus();
        return;
    }
    const data = {
        memAccount: document.getElementById('memAccount').value.trim(),
        memEmail: emailInput.value.trim(),
        memName: document.getElementById('memName').value.trim(),
        memNickName: document.getElementById('memNickName').value.trim(),
        memBirthday: document.getElementById('memBirthday').value,
        memAddr: document.getElementById('memAddr').value.trim(),
        memPhone: document.getElementById('memPhone').value.trim(),
        memPassword: memPassword
    };
    try {
        const resp = await fetch('/api/members/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await resp.json();
        if (result.success) {
            alert('註冊成功！歡迎加入 PIXEL TRIBE！');
            window.location.href = '/index.html';
        } else {
            alert(result.message);
        }
    } catch (err) {
        alert("系統忙碌或網路異常，請稍後再試！");
        console.error(err);
    }
});