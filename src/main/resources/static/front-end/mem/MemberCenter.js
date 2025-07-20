document.addEventListener('DOMContentLoaded', async function() {
	// ======== 1. 會員認證 ========
	const jwt = localStorage.getItem('jwt');
	const memberInfoRaw = localStorage.getItem('memberInfo');
	let memberInfo = null;
	let memberId = null;

	function isJwtExpired(token) {
		try {
			const payload = JSON.parse(atob(token.split('.')[1]));
			if (!payload.exp) return false;
			const now = Math.floor(Date.now() / 1000);
			return payload.exp < now;
		} catch (e) {
			return true;
		}
	}

	if (!jwt || !memberInfoRaw || isJwtExpired(jwt)) {
		alert('請先登入會員');
		window.location.href = '/front-end/mem/MemberLogin.html';
		return;
	}
	try {
		memberInfo = JSON.parse(memberInfoRaw);
		memberId = memberInfo.id;
	} catch (e) {
		alert('找不到會員資訊，請重新登入');
		window.location.href = '/front-end/mem/MemberLogin.html';
		return;
	}
	if (!memberId) {
		alert('找不到會員資訊，請重新登入');
		window.location.href = '/front-end/mem/MemberLogin.html';
		return;
	}

	// ======== 2. 取得會員資料 ========
	let memberProfileDTO = null;
	try {
		const res = await fetch(`/api/members/profile/${memberId}`, {
			method: 'GET',
			headers: { 'Authorization': 'Bearer ' + jwt }
		});
		if (!res.ok) throw new Error('無權限或已登出');
		memberProfileDTO = await res.json();
	} catch (e) {
		alert('登入已失效，請重新登入');
		localStorage.removeItem('jwt');
		localStorage.removeItem('memberInfo');
		window.location.href = '/front-end/mem/MemberLogin.html';
		return;
	}

	window.memberProfile = memberProfileDTO;

	// ======== 3. 動態渲染視圖定義 ========
	const memberViews = {
		profile: function(member, editMode = false) {
			const avatarSrc = `/uploads/memberAvatar/${member.memIconData || 'defaultmem.png'}`;
			if (!editMode) {
				return `
          <div class="member-center-card mb-4 bg-white">
            <div class="card-body py-4 px-4">
              <div class="d-flex align-items-center mb-3">
                <img src="${avatarSrc}" alt="會員頭像" class="avatar-lg me-3" id="member-avatar" onerror="this.onerror=null;this.src='/images/memberAvatar/defaultmem.png'">
              </div>
              <dl class="profile-list">
                <dt>姓名:</dt><dd>${member.memName || '-'}</dd>
                <dt>生日:</dt><dd>${member.memBirthday || '-'}</dd>
                <dt>帳號:</dt><dd>${member.memAccount || '-'}</dd>
                <dt>暱稱:</dt><dd>${member.memNickName || '-'}</dd>
                <dt>信箱:</dt><dd>${member.memEmail || '-'}</dd>
                <dt>地址:</dt><dd>${member.memAddr || '-'}</dd>
                <dt>手機:</dt><dd>${member.memPhone || '-'}</dd>
              </dl>
              <div class="profile-edit-btns mt-3">
                <button class="btn btn-main" id="editProfileBtn"><i class="bi bi-pencil"></i> 修改個人資料</button>
              </div>
            </div>
          </div>
          <div class="member-center-card mb-4 bg-white">
            <div class="card-body py-4 px-4">
              <div class="card-section-title mb-2">密碼重設:</div>
			  <div class="card-section-title mb-2">請輸入8碼以上，含英文及數字的密碼</div>
			  <form id="resetPwdForm" autocomplete="off">
			    <input type="password" id="oldPassword" class="form-control mb-2" placeholder="輸入原密碼" required>
			    <div class="invalid-feedback" id="oldPasswordError"></div>
			    <input type="password" id="newPassword" class="form-control mb-2" placeholder="輸入新密碼" required>
			    <div class="invalid-feedback" id="newPasswordError"></div>
			    <input type="password" id="newPasswordConfirm" class="form-control mb-3" placeholder="再次輸入新密碼" required>
			    <div class="invalid-feedback" id="newPasswordConfirmError"></div>
			    <div class="d-flex mb-2" style="gap:10px;">
			      <button type="submit" class="btn btn-main flex-fill">重設密碼</button>
			    </div>
			    <div id="resetPwdMsg" class="text-center mt-2"></div>
			  </form>
            </div>
          </div>
        `;
			} else {
				return `
          <div class="member-center-card mb-4 bg-white">
            <div class="card-body py-4 px-4">
			<div class="avatar-upload-wrapper">
			  <input type="file" id="avatarUploadInput" accept="image/*" style="display:none" />
			  <div class="avatar-hover-btn" id="avatarUploadBtn">
			    <img src="${avatarSrc}" alt="會員頭像" class="avatar-lg" id="member-avatar" onerror="this.onerror=null;this.src='/images/memberAvatar/defaultmem.png'">
			    <div class="avatar-hover-mask">
			      <i class="bi bi-camera"></i><span>更換頭像</span>
			    </div>
			  </div>
			</div>
              <form id="editProfileForm" autocomplete="off">
                <dl class="profile-list">
                  <dt>姓名:</dt>
                  <dd style="display: flex; flex-direction: column;">
                    <input class="form-control edit-field" name="memName" value="${member.memName || ''}" required>
                    <div class="invalid-feedback" id="memNameError"></div>
                  </dd>
                  <dt>生日:</dt>
                  <dd>${member.memBirthday || '-'}</dd>
                  <dt>帳號:</dt>
                  <dd>${member.memAccount || '-'}</dd>
                  <dt>暱稱:</dt>
                  <dd style="display: flex; flex-direction: column;">
                    <input class="form-control edit-field" name="memNickName" value="${member.memNickName || ''}">
                    <div class="invalid-feedback" id="memNickNameError"></div>
                  </dd>
                  <dt>信箱:</dt>
                  <dd style="display: flex; flex-direction: column;">
                    <input class="form-control edit-field" name="memEmail" type="email" value="${member.memEmail || ''}" required>
                    <div class="invalid-feedback" id="memEmailError"></div>
                  </dd>
                  <dt style="display: flex; flex-direction: column;">地址:</dt>
                  <dd>
                    <input class="form-control edit-field" name="memAddr" value="${member.memAddr || ''}">
                  </dd>
                  <dt>手機:</dt>
                  <dd style="display: flex; flex-direction: column;">
                    <input class="form-control edit-field" name="memPhone" value="${member.memPhone || ''}">
                    <div class="invalid-feedback" id="memPhoneError"></div>
                  </dd>
                </dl>
                <div id="editProfileMsg" class="text-center mt-2"></div>
                <div class="profile-edit-btns mt-3">
                  <button type="submit" class="btn btn-main"><i class="bi bi-check-lg"></i> 儲存改動</button>
                  <button type="button" class="btn btn-outline-secondary" id="cancelEditBtn"><i class="bi bi-x-lg"></i> 取消</button>
                </div>
              </form>
            </div>
          </div>
          <div class="member-center-card mb-4 bg-white">
            <div class="card-body py-4 px-4">
              <div class="card-section-title mb-2">密碼重設</div>
			  <div class="card-section-title mb-2">請輸入8碼以上，含英文及數字的密碼</div>
			  <form id="resetPwdForm" autocomplete="off">
			    <input type="password" id="oldPassword" class="form-control mb-2" placeholder="輸入原密碼" required>
			    <div class="invalid-feedback" id="oldPasswordError"></div>
			    <input type="password" id="newPassword" class="form-control mb-2" placeholder="輸入新密碼" required>
			    <div class="invalid-feedback" id="newPasswordError"></div>
			    <input type="password" id="newPasswordConfirm" class="form-control mb-3" placeholder="再次輸入新密碼" required>
			    <div class="invalid-feedback" id="newPasswordConfirmError"></div>
			    <div class="d-flex mb-2" style="gap:10px;">
			      <button type="submit" class="btn btn-main flex-fill">重設密碼</button>
			    </div>
			    <div id="resetPwdMsg" class="text-center mt-2"></div>
			  </form>
            </div>
          </div>
        `;
			}
		},
		orders: async function(member) {
			let html = `
		        <div class="member-center-card mb-4 bg-white">
		          <div class="card-body py-4 px-4">
		            <div class="card-section-title">消費紀錄</div>
		            <div class="table-responsive">
		              <table class="table align-middle mb-0">
		                <thead>
		                  <tr>
		                    <th>訂單編號</th>
		                    <th>訂購時間</th>
		                    <th>訂單狀態</th>
		                  </tr>
		                </thead>
		                <tbody id="orders-tbody">
		                  <tr><td colspan="4" class="text-center text-muted">資料載入中...</td></tr>
		                </tbody>
		              </table>
		            </div>
		          </div>
		        </div>
		      `;
			document.getElementById('memberMainArea').innerHTML = html;

			const jwt = localStorage.getItem('jwt');
			const id = memberInfo.id;
			try {
				const res = await fetch(`/api/orders/member/${id}`, {
					method: 'GET',
					headers: { 'Authorization': 'Bearer ' + jwt }
				});
				const orders = await res.json();

				let rows = '';
				if (orders && orders.length > 0) {
					rows = orders.map(order => `
		                <tr>
		                  <td>${order.orderNo}</td>
		                  <td>${order.orderDatetime ? order.orderDatetime.replace('T', ' ').slice(0, 16) : ''}</td>
		                  <td>${order.orderStatus || '-'}</td>
		                </tr>
		              `).join('');
				} else {
					rows = `<tr><td colspan="4" class="text-center text-muted">您目前尚未有任何訂單紀錄</td></tr>`;
				}
				document.getElementById('orders-tbody').innerHTML = rows;
			} catch (e) {
				document.getElementById('orders-tbody').innerHTML = `<tr><td colspan="4" class="text-danger text-center">資料取得失敗</td></tr>`;
			}
		},
		coupons: async function(member) {
			let html = `
		        <div class="member-center-card mb-4 bg-white">
		          <div class="card-body py-4 px-4">
		            <div class="card-section-title">我的優惠券</div>
		            <div class="table-responsive">
		              <table class="table align-middle mb-0">
		                <thead>
		                  <tr>
		                    <th>名稱</th>
		                    <th>序號</th>
		                    <th>適用範圍</th>
		                    <th>到期日</th>
		                    <th>狀態</th>
		                  </tr>
		                </thead>
		                <tbody id="coupons-tbody">
		                  <tr><td colspan="5" class="text-center text-muted">資料載入中...</td></tr>
		                </tbody>
		              </table>
		            </div>
		          </div>
		        </div>
		      `;
			document.getElementById('memberMainArea').innerHTML = html;

			const jwt = localStorage.getItem('jwt');
			const id = memberInfo.id;
			try {
				const res = await fetch(`/api/coupon/member/${id}`, {
					method: 'GET',
					headers: { 'Authorization': 'Bearer ' + jwt }
				});
				const coupons = await res.json();

				let rows = '';
				if (coupons && coupons.length > 0) {
					rows = coupons.map(coupon => `
		                <tr>
		                  <td>${coupon.couName || '-'}</td>
		                  <td>${coupon.couCode || '-'}</td>
		                  <td>全部商品</td>
		                  <td>${coupon.couUseEnd ? coupon.couUseEnd.replace('T', ' ').slice(0, 10) : '-'}</td>
		                  <td>${coupon.couStatus === '1' ? '已使用' : '未使用'}</td>
		                </tr>
		              `).join('');
				} else {
					rows = `<tr><td colspan="5" class="text-center text-muted">您尚未擁有任何優惠券</td></tr>`;
				}
				document.getElementById('coupons-tbody').innerHTML = rows;
			} catch (e) {
				document.getElementById('coupons-tbody').innerHTML = `<tr><td colspan="5" class="text-danger text-center">資料取得失敗</td></tr>`;
			}
		},
	};

	// 集中綁定所有動態事件
	function profileViewHandler() {
		const editBtn = document.getElementById('editProfileBtn');
		if (editBtn) {
			editBtn.removeEventListener('click', editBtn._handler);
			const handler = () => renderTab('profile', true);
			editBtn.addEventListener('click', handler);
			editBtn._handler = handler;
		}
	}

	function profileEditHandler() {
		// 取消編輯
		const cancelBtn = document.getElementById('cancelEditBtn');
		if (cancelBtn) {
			cancelBtn.removeEventListener('click', cancelBtn._handler);
			const cancelHandler = () => renderTab('profile', false);
			cancelBtn.addEventListener('click', cancelHandler);
			cancelBtn._handler = cancelHandler;
		}
		const form = document.getElementById('editProfileForm');
		if (!form) return;

		// 欄位與錯誤區
		const nameInput = form.querySelector('[name="memName"]');
		const nickInput = form.querySelector('[name="memNickName"]');
		const emailInput = form.querySelector('[name="memEmail"]');
		const phoneInput = form.querySelector('[name="memPhone"]');
		const nameError = document.getElementById('memNameError');
		const nickError = document.getElementById('memNickNameError');
		const emailError = document.getElementById('memEmailError');
		const phoneError = document.getElementById('memPhoneError');
		const msg = document.getElementById('editProfileMsg');

		const chineseOnly = /^[\u4e00-\u9fa5]{2,10}$/;
		const emailPattern = /^[A-Za-z0-9._%+-]+@(?:gmail\.com|yahoo\.com(\.tw)?|hotmail\.com|outlook\.com|icloud\.com|msn\.com|hotmail\.com\.tw)$/;
		const phonePattern = /^09\d{8}$/;

		// 即時驗證
		nameInput.addEventListener('input', validate);
		nickInput.addEventListener('input', validate);
		emailInput.addEventListener('input', validate);
		phoneInput.addEventListener('input', validate);

		function validate() {
			let valid = true;
			nameError.textContent = '';
			nickError.textContent = '';
			emailError.textContent = '';
			phoneError.textContent = '';
			msg.textContent = '';
			nameInput.classList.remove('is-invalid');
			nickInput.classList.remove('is-invalid');
			emailInput.classList.remove('is-invalid');
			phoneInput.classList.remove('is-invalid');
			if (!chineseOnly.test(nameInput.value)) {
				nameError.textContent = '姓名請填入中文';
				nameInput.classList.add('is-invalid');
				valid = false;
			}
			if (nickInput.value && !chineseOnly.test(nickInput.value)) {
				nickError.textContent = '暱稱請填入中文';
				nickInput.classList.add('is-invalid');
				valid = false;
			}
			if (!emailPattern.test(emailInput.value)) {
				emailError.textContent = '請確認信箱格式';
				emailInput.classList.add('is-invalid');
				valid = false;
			}
			if (phoneInput.value && !phonePattern.test(phoneInput.value)) {
				phoneError.textContent = '手機格式應為:09xx-xxx-xxx';
				phoneInput.classList.add('is-invalid');
				valid = false;
			}
			return valid;
		}

		form.removeEventListener('submit', form._handler);

		const submitHandler = async function(e) {
			e.preventDefault();
			msg.textContent = '';
			msg.style.color = '#b94040';

			if (!validate()) {
				msg.textContent = '請確認各欄位資料無誤後再送出';
				return;
			}

			const formData = new FormData(form);
			const payload = {};
			['memName', 'memNickName', 'memEmail', 'memAddr', 'memPhone'].forEach(key => {
				payload[key] = formData.get(key) ? formData.get(key).trim() : '';
			});
			if (!payload.memName || !payload.memEmail) {
				msg.textContent = '請填寫必填欄位';
				return;
			}
			msg.textContent = '儲存中...';
			msg.style.color = '#555';

			try {
				const jwt = localStorage.getItem('jwt');
				const memberInfo = JSON.parse(localStorage.getItem('memberInfo'));
				const memberId = memberInfo.id;
				const res = await fetch(`/api/members/editProfile/${memberId}`, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json',
						'Authorization': 'Bearer ' + jwt
					},
					body: JSON.stringify(payload)
				});
				const result = await res.json();

				if (!res.ok || !result.success) {
					msg.style.color = '#b94040';
					msg.textContent = result.message || '更新失敗，請稍後再試';
					return;
				}

				Object.assign(window.memberProfile, payload);
				msg.style.color = '#25945a';
				msg.textContent = '更新成功';
				setTimeout(() => renderTab('profile', false), 700);

			} catch (e) {
				msg.style.color = '#b94040';
				msg.textContent = '更新失敗，請稍後再試';
			}
		};
		form.addEventListener('submit', submitHandler);
		form._handler = submitHandler;
	}

	function memPasswordReset() {
		const form = document.getElementById('resetPwdForm');
		if (!form) return;
		const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d\W_]{8,}$/;
		const oldPassword = document.getElementById('oldPassword');
		const newPassword = document.getElementById('newPassword');
		const newPasswordConfirm = document.getElementById('newPasswordConfirm');
		const oldPasswordError = document.getElementById('oldPasswordError');
		const newPasswordError = document.getElementById('newPasswordError');
		const newPasswordConfirmError = document.getElementById('newPasswordConfirmError');
		const msg = document.getElementById('resetPwdMsg');

		newPassword && newPassword.addEventListener('input', validatePasswords);
		newPasswordConfirm && newPasswordConfirm.addEventListener('input', validatePasswords);

		function validatePasswords() {
			let valid = true;
			newPasswordError.textContent = '';
			newPasswordConfirmError.textContent = '';
			newPassword.classList.remove('is-invalid');
			newPasswordConfirm.classList.remove('is-invalid');
			msg.textContent = '';

			if (!passwordPattern.test(newPassword.value)) {
				newPasswordError.textContent = '新密碼格式不符';
				newPassword.classList.add('is-invalid');
				valid = false;
			}
			if (newPassword.value !== newPasswordConfirm.value) {
				newPasswordConfirmError.textContent = '請確認兩次輸入的新密碼是否一致';
				newPasswordConfirm.classList.add('is-invalid');
				valid = false;
			}
			return valid;
		}

		form.removeEventListener('submit', form._handler);
		const resetHandler = function(e) {
			e.preventDefault();
			oldPasswordError.textContent = '';
			newPasswordError.textContent = '';
			newPasswordConfirmError.textContent = '';
			msg.textContent = '';
			oldPassword.classList.remove('is-invalid');
			newPassword.classList.remove('is-invalid');
			newPasswordConfirm.classList.remove('is-invalid');

			if (!oldPassword.value || !newPassword.value || !newPasswordConfirm.value) {
				msg.textContent = '請填寫所有欄位';
				msg.style.color = '#b94040';
				if (!oldPassword.value) oldPassword.classList.add('is-invalid');
				if (!newPassword.value) newPassword.classList.add('is-invalid');
				if (!newPasswordConfirm.value) newPasswordConfirm.classList.add('is-invalid');
				return;
			}
			if (!validatePasswords()) {
				msg.textContent = '請正確填寫欄位後再送出';
				msg.style.color = '#b94040';
				return;
			}
			const id = memberInfo.id;
			if (!id) {
				msg.textContent = '請重新登入以修改密碼';
				msg.style.color = '#b94040';
				return;
			}
			fetch('/api/members/reset-password', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': 'Bearer ' + jwt
				},
				body: JSON.stringify({
					id: id,
					oldPassword: oldPassword.value.trim(),
					newPassword: newPassword.value.trim(),
					newPasswordConfirm: newPasswordConfirm.value.trim()
				})
			})
				.then(res => res.json())
				.then(data => {
					if (data.success) {
						alert('密碼已更新');
						form.reset();
					} else {
						msg.style.color = '#b94040';
						msg.textContent = data.message || '密碼重設失敗';
					}
				})
				.catch(() => {
					msg.style.color = '#b94040';
					msg.textContent = '系統錯誤，請稍後再試';
				});
		};
		form.addEventListener('submit', resetHandler);
		form._handler = resetHandler;
	}

	// 頭像上傳處理
	function bindAvatarUpload() {
		const avatarBtn = document.getElementById('avatarUploadBtn');
		const avatarInput = document.getElementById('avatarUploadInput');
		const avatarImg = document.getElementById('member-avatar');
		if (!avatarBtn || !avatarInput || !avatarImg) return;

		// 點頭像按鈕觸發 file input
		avatarBtn.addEventListener('click', () => {
			avatarInput.click();
		});

		// 上傳圖檔即時預覽
		avatarInput.addEventListener('change', function() {
			const file = this.files[0];
			if (!file) return;
			if (!file.type.match(/^image\//)) {
				alert('請選擇圖片檔案！');
				return;
			}
			if (file.size > 2 * 1024 * 1024) { // 2MB
				alert('檔案太大，請選擇 2MB 以內圖片！');
				return;
			}
			const reader = new FileReader();
			reader.onload = function(e) {
				avatarImg.src = e.target.result; // 預覽
			}
			reader.readAsDataURL(file);

			uploadAvatar(file);
		});
	}

	// 上傳頭像到後端
	function uploadAvatar(file) {
		const jwt = localStorage.getItem('jwt');
		const memberInfo = JSON.parse(localStorage.getItem('memberInfo'));
		const memberId = memberInfo.id;
		const formData = new FormData();
		formData.append('avatar', file);

		fetch(`/api/members/${memberId}/avatar`, {
			method: 'POST',
			headers: {
				'Authorization': 'Bearer ' + jwt
			},
			body: formData
		})
			.then(res => res.json())
			.then(data => {
				if (data.success) {
					alert('頭像上傳成功');
					fetch(`/api/members/profile/${memberId}`, {
						method: 'GET',
						headers: { 'Authorization': 'Bearer ' + jwt }
					})
						.then(res => res.json())
						.then(newMemberInfo => {
							localStorage.setItem('memberInfo', JSON.stringify(newMemberInfo));
							window.memberProfile = newMemberInfo;
							document.getElementById('member-avatar').src = `/uploads/memberAvatar/${newMemberInfo.memIconData || 'defaultmem.png'}?t=${Date.now()}`;
						});
				} else {
					alert(data.message || '頭像上傳失敗');
				}
			})
			.catch(() => {
				alert('系統錯誤，上傳失敗');
			});
	}

	async function renderTab(tab, editMode = false) {
		const url = new URL(window.location);
		url.searchParams.set('tab', tab);
		history.replaceState(null, '', url);
		document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.toggle('active', btn.dataset.tab === tab));
		if (tab === 'profile') {
			document.getElementById('memberMainArea').innerHTML = memberViews.profile(window.memberProfile, editMode);
			if (editMode) {
				profileEditHandler();
				bindAvatarUpload();
			} else {
				profileViewHandler();
			}
			memPasswordReset();
		} else {
			const view = memberViews[tab];
			if (typeof view === "function") {
				if (view.constructor.name === "AsyncFunction") {
					document.getElementById('memberMainArea').innerHTML = `<div style="text-align:center;padding:48px 0;">載入中...</div>`;
					await view(window.memberProfile);
				} else {
					document.getElementById('memberMainArea').innerHTML = view(window.memberProfile);
				}
			} else {
				document.getElementById('memberMainArea').innerHTML = view || '<div>找不到資料</div>';
			}
		}
		initAllPageEvent();
	}

	function initAllPageEvent() {
		profileViewHandler();
		profileEditHandler();
		memPasswordReset();
	}

	document.querySelectorAll('.tab-btn').forEach(btn => {
		btn.addEventListener('click', () => renderTab(btn.dataset.tab));
	});

	const urlParams = new URLSearchParams(window.location.search);
	const defaultTab = urlParams.get('tab') || 'profile';
	await renderTab(defaultTab, false);

});
