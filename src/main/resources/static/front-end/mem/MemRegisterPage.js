let emailIsUnique = false;

// 1. 中文驗證 (姓名/暱稱/地址)
function validateName() {
	const nameInput = document.getElementById('memName');
	const nameMsg = document.getElementById('nameMsg');
	const regex = /^[\u4e00-\u9fa5]{2,10}$/; //1~10中文
	if (!regex.test(nameInput.value.trim())) {
		nameMsg.textContent = '❌請使用中文姓名';
		nameMsg.style.color = "#ad3131";
		return false;
	} else {
		nameMsg.textContent = '\u00A0✅\u00A0+1';
		nameMsg.style.color = "#31ad50";
		return true;
	}
}

function validateNickName() {
	const nickInput = document.getElementById('memNickName');
	const nickMsg = document.getElementById('nickNameMsg');
	const regex = /^[\u4e00-\u9fa5A-Za-z0-9]{1,15}$/; // 1~15中英文、數字
	if (!regex.test(nickInput.value.trim())) {
		nickMsg.textContent = '❌請輸入1~15個中英文、數字作為暱稱';
		nickMsg.style.color = "#ad3131";
		return false;
	} else {
		nickMsg.textContent = '\u00A0✅\u00A0+1';
		nickMsg.style.color = "#31ad50";
		return true;
	}
}

function validateAddr() {
	const addrInput = document.getElementById('memAddr');
	const addrMsg = document.getElementById('addrMsg');
	const regex = /^[\u4e00-\u9fa50-9\-，。、\s]{6,}$/; // 至少6碼、可含中文/數字/空格/常見符號
	if (!regex.test(addrInput.value.trim())) {
		addrMsg.textContent = '❌請用中文及阿拉伯數字填寫';
		addrMsg.style.color = "#ad3131";
		return false;
	} else {
		addrMsg.textContent = '\u00A0✅\u00A0+1';
		addrMsg.style.color = "#31ad50";
		return true;
	}
}

// 2. 電話驗證
function validatePhone() {
	const phoneInput = document.getElementById('memPhone');
	const phoneMsg = document.getElementById('phoneMsg');
	const regex = /^09\d{2}\d{3}\d{3}$/;
	if (!regex.test(phoneInput.value.trim())) {
		phoneMsg.textContent = '❌請使用格式為09xx-xxx-xxx的號碼';
		phoneMsg.style.color = "#ad3131";
		return false;
	} else {
		phoneMsg.textContent = '\u00A0✅\u00A0+1';
		phoneMsg.style.color = "#31ad50";
		return true;
	}
}

// 3. 密碼：8碼以上 英文+數字(可含符號)
function validatePassword() {
	const pwdInput = document.getElementById('memPassword');
	const pwdMsg = document.getElementById('passwordMsg');
	const regex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d\S]{8,}$/;
	if (!regex.test(pwdInput.value.trim())) {
		pwdMsg.textContent = '❌請設定8碼以上含英文、數字的密碼';
		pwdMsg.style.color = "#ad3131";
		return false;
	} else {
		pwdMsg.textContent = '\u00A0✅\u00A0+1';
		pwdMsg.style.color = "#31ad50";
		return true;
	}
}

// 4. 註冊時再次檢查
document.getElementById('memRegister').addEventListener('submit', async function(e) {
	e.preventDefault();

	// 每個驗證都要通過
	if (!accountIsUnique||
		!validateName() ||
		!validateNickName() ||
		!validateAddr() ||
		!validatePhone() ||
		!validatePassword() ||
		!emailIsUnique
	) {
		alert('請確認所有欄位格式正確！');
		return;
	}

	// 密碼一致性檢查（你的原本有寫，可以保留）
	const memPassword = document.getElementById('memPassword').value;
	const confirmPassword = document.getElementById('confirmPassword').value;
	if (memPassword !== confirmPassword) {
		alert('密碼與確認密碼不一致！');
		document.getElementById('confirmPassword').focus();
		return;
	}

});

//驗證帳號是否重複
let accountIsUnique = false;
async function checkAccountDuplicate() {
    const accountInput = document.getElementById('memAccount');
    const accountMsg = document.getElementById('accountMsg');
    const account = accountInput.value.trim();
    // 格式限制：6~16位英數字（可微調）
    const regex = /^[A-Za-z0-9]{6,16}$/;
    if (!account) {
        accountMsg.textContent = '❌請輸入帳號';
        accountMsg.style.color = "#ad3131";
        accountIsUnique = false;
        return;
    } else if (!regex.test(account)) {
        accountMsg.textContent = '❌帳號需6-16位英文或數字';
        accountMsg.style.color = "#ad3131";
        accountIsUnique = false;
        return;
    }
    try {
        const resp = await fetch('/api/members/check-account', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ account })
        });
        const result = await resp.json();
        if (result.exist) {
            accountMsg.textContent = '❌帳號已有人使用';
            accountMsg.style.color = "#ad3131";
            accountIsUnique = false;
        } else {
            accountMsg.textContent = '\u00A0✅\u00A0+1';
            accountMsg.style.color = "#31ad50";
            accountIsUnique = true;
        }
    } catch (err) {
        accountMsg.textContent = '無法檢查帳號，請稍後再試';
        accountMsg.style.color = "#c14444";
        accountIsUnique = false;
    }
}

//驗證email是否重複
async function checkEmailDuplicate() {
	const emailInput = document.getElementById('memEmail');
	const emailMsg = document.getElementById('emailMsg');
	const email = emailInput.value.trim();
	if (!email) {
		emailMsg.textContent = '❌請填入信箱';
		emailMsg.style.color = "#ad3131"
		emailIsUnique = false;
		return;
	} else if (!/^[A-Za-z0-9._%+-]+@(?:gmail\.com|yahoo\.com(\.tw)?|hotmail\.com|outlook\.com|icloud\.com|msn\.com|hotmail\.com\.tw)$/i.test(email)) {
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
			emailMsg.textContent = '❌帳號已有人使用';
			emailMsg.style.color = "#ad3131"
			emailIsUnique = false;
		} else {
			emailMsg.textContent = '\u00A0✅\u00A0+1';
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