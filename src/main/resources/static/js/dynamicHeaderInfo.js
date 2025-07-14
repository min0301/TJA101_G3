(function() {
	// ====== 1. 顯示像素風登出燈箱 ======
	function showPixelModal(msg, callback) {
		// 如果已存在 modal 則移除
		let oldModal = document.getElementById('pixel-logout-modal');
		if (oldModal) oldModal.remove();

		// 遮罩
		const modal = document.createElement('div');
		modal.id = 'pixel-logout-modal';
		modal.style.position = 'fixed';
		modal.style.left = 0;
		modal.style.top = 0;
		modal.style.width = '100vw';
		modal.style.height = '100vh';
		modal.style.background = 'rgba(30,28,38,0.88)';
		modal.style.display = 'flex';
		modal.style.alignItems = 'center';
		modal.style.justifyContent = 'center';
		modal.style.zIndex = 9999;

		// 內容框
		const box = document.createElement('div');
		box.style.background = '#18122B';
		box.style.border = '4px solid #FFD700';
		box.style.borderRadius = '18px';
		box.style.padding = '44px 44px 36px 44px';
		box.style.boxShadow = '0 8px 32px #000A';
		box.style.textAlign = 'center';
		box.style.maxWidth = '90vw';

		// 像素字
		const content = document.createElement('div');
		content.style.fontFamily = '"Press Start 2P", monospace';
		content.style.fontSize = '2rem';
		content.style.color = '#FFD700';
		content.style.textShadow = '1px 2px 0 #473638';
		content.textContent = msg;

		// OK按鈕
		const btn = document.createElement('button');
		btn.textContent = 'OK';
		btn.style.fontFamily = '"Press Start 2P", monospace';
		btn.style.fontSize = '1rem';
		btn.style.marginTop = '32px';
		btn.style.padding = '10px 32px';
		btn.style.background = '#FFD700';
		btn.style.color = '#18122B';
		btn.style.border = 'none';
		btn.style.borderRadius = '8px';
		btn.style.cursor = 'pointer';
		btn.style.letterSpacing = '1px';
		btn.style.transition = 'background 0.2s';
		btn.onmouseenter = function() { btn.style.background = '#FFA500'; }
		btn.onmouseleave = function() { btn.style.background = '#FFD700'; }
		btn.onclick = function() {
			modal.remove();
			if (typeof callback === 'function') callback();
		};

		box.appendChild(content);
		box.appendChild(btn);
		modal.appendChild(box);
		document.body.appendChild(modal);
	}

	// ====== 2. 動態渲染 header 資訊 ======
	function tryRenderHeaderInfo() {
		const memberInfo = JSON.parse(localStorage.getItem('memberInfo'));
		const jwt = localStorage.getItem('jwt');

		const memberArea = document.getElementById('header-member-area');
		const guestArea = document.getElementById('header-guest-area');
		const addPostBtn = document.getElementById('addPostStart');
		const login = document.getElementById('login');
		const signup = document.getElementById('signup');
		if (memberInfo && jwt) {
			// 顯示會員，隱藏訪客
			if (memberArea) memberArea.style.display = "";
			if (guestArea) guestArea.style.display = "none";
			if (addPostBtn) addPostBtn.style.display = "";
			if (login) login.style.display = "none";
			if (signup) signup.style.display = "none";
			
			// 填資料
			const listMemName = document.getElementById('listMemName');
			const listMemEmail = document.getElementById('listMemEmail');
			const headerMemNickName = document.getElementById('headerMemNickName');
			const avatarImg = document.getElementById('headerAvatar');
			if (listMemName) listMemName.textContent = memberInfo.memName;
			if (listMemEmail) listMemEmail.textContent = memberInfo.memEmail;
			if (headerMemNickName) headerMemNickName.textContent = memberInfo.memNickName;
			if (avatarImg) {
				avatarImg.src = `/images/memberAvatar/mem${memberInfo.id}.png`;
				avatarImg.onerror = function() {
					this.onerror = null;
					this.src = '/images/memberAvatar/default.png';
				};
			}
		} else {
			// 顯示訪客，隱藏會員
			if (memberArea) memberArea.style.display = "none";
			if (guestArea) guestArea.style.display = "flex";
			if (addPostBtn) addPostBtn.style.display = "none";
		}
	}

	// ====== 3. 綁定 logout 按鈕 ======
	function bindLogoutBtn() {
		const logoutBtn = document.getElementById('logout-btn');
		if (logoutBtn && !logoutBtn.hasAttribute('data-bound')) {
			logoutBtn.setAttribute('data-bound', 'true');
			logoutBtn.addEventListener('click', function(e) {
				e.preventDefault();
				showPixelModal('GoodBye Pixi!', function() {
					localStorage.removeItem('memberInfo');
					localStorage.removeItem('jwt');
					window.location.href = '/index.html';
				});
			});
		}
	}

	// ====== 4. 等待 header 載入後統一處理 ======
	function waitHeaderAndRender() {
		const header =
			document.getElementById('header-placeholder') ||
			document.querySelector('header');
		// 偵測至少一個必要欄位出現就開始（支援動態載入 header）
		if (
			header &&
			(document.getElementById('listMemName') ||
				document.getElementById('headerMemNickName') ||
				document.getElementById('headerAvatar') ||
				document.getElementById('logout-btn'))
		) {
			tryRenderHeaderInfo();
			bindLogoutBtn();
		} else {
			setTimeout(waitHeaderAndRender, 80); // 每80ms檢查一次
		}
	}

	// ====== 5. 頁面載入自動啟動 ======
	document.addEventListener('DOMContentLoaded', waitHeaderAndRender);

})();
