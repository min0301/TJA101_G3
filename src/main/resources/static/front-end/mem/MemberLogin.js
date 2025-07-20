document.getElementById('loginForm').addEventListener('submit', function(e) {
	e.preventDefault();
	const memAccount = document.getElementById('memAccount').value.trim();
	const memPassword = document.getElementById('memPassword').value.trim();
	const result = document.getElementById('result');
	if (!memAccount && !memPassword) {
		result.textContent = '請輸入帳號及密碼';
		return;
	} else if (!memAccount) {
		result.textContent = '請輸入帳號';
		return;
	} else if (!memPassword) {
		result.textContent = '請輸入密碼';
		return;
	}
	fetch('/api/mem/login', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ memAccount, memPassword })
	})
		.then(res => res.json())
		.then(data => {
			// 登入成功
			if (data.success && data.token) {
				alert('登入成功！');
				localStorage.setItem('jwt', data.token);
				localStorage.setItem('memberInfo', JSON.stringify(data.memberInfo));
				window.location.href = "/index.html";
			} else if (data.message && data.message.includes('停權')) {
				alert('死小孩已被停權');
				window.location.href = "/index.html";
			} else {
				result.textContent = data.message;
			}
		});
});

// =============== 忘記密碼 Modal 開關 ===============
document.getElementById('forgotLink').onclick = function() {
	document.getElementById('forgotModal').style.display = 'flex';
	document.getElementById('modalResult').textContent = '';
	document.getElementById('modalResult').style.color = '#b94040';
	document.getElementById('loadingBox').style.display = 'none';
	document.getElementById('forgotEmail').value = '';
};
document.getElementById('closeModal').onclick = function() {
	document.getElementById('forgotModal').style.display = 'none';
	document.getElementById('modalResult').textContent = '';
	document.getElementById('loadingBox').style.display = 'none';
	document.getElementById('forgotEmail').value = '';
};
document.getElementById('forgotModal').onclick = function(e) {
	if (e.target === this) {
		this.style.display = 'none';
		document.getElementById('modalResult').textContent = '';
		document.getElementById('loadingBox').style.display = 'none';
		document.getElementById('forgotEmail').value = '';
	}
};

// =============== 寄送認證信功能與Loading提示 ===============
document.getElementById('sendMailBtn').onclick = function() {
	const email = document.getElementById('forgotEmail').value.trim();
	const result = document.getElementById('modalResult');
	const loadingBox = document.getElementById('loadingBox');
	result.textContent = "";
	result.style.color = "";
	if (!email) {
		result.textContent = '請輸入Email';
		result.style.color = "#b94040";
		return;
	}
	loadingBox.style.display = 'block';
	fetch('/api/members/forgot-password', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ email })
	})
		.then(res => res.json())
		.then(data => {
			loadingBox.style.display = 'none';
			if (data.success) {
				result.style.color = "#25a45a";
				result.textContent = data.message || '認證信已寄出，請至信箱查收！';
				localStorage.setItem('emailForReset', email);
				setTimeout(() => {
					window.location.href = "/front-end/mem/PasswordResetWithVcode.html";
				}, 1500);
			} else {
				result.style.color = "#b94040";
				result.textContent = data.message || '寄送失敗，請確認Email';
			}
		})
		.catch(() => {
			loadingBox.style.display = 'none';
			result.style.color = "#b94040";
			result.textContent = "系統異常，請稍後再試";
		});
};