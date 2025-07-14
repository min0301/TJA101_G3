(function() {
  // 檢查 header 是否已經在頁面上
  function tryRenderHeaderInfo() {
    const memberInfo = JSON.parse(localStorage.getItem('memberInfo'));
    if (!memberInfo) return;

    // 依實際 header 結構，調整這些 id
    const listMemName = document.getElementById('listMemName');
    const listMemEmail = document.getElementById('listMemEmail');
    const headerMemNickName = document.getElementById('headerMemNickName');
    const avatarImg = document.getElementById('headerAvatar');

    if (listMemName) listMemName.textContent = memberInfo.memName;
    if (listMemEmail) listMemEmail.textContent = memberInfo.memEmail;
    if (headerMemNickName) headerMemNickName.textContent = memberInfo.memNickName;
    if (avatarImg) avatarImg.src = `/images/memberAvatar/mem${memberInfo.id}.png`;
  }

  // 判斷 header 是否已經載入進 DOM
  function waitHeaderAndRender() {
    const header =
      document.getElementById('header-placeholder') ||
      document.querySelector('header');
    if (
      header &&
      (document.getElementById('listMemName') ||
        document.getElementById('headerMemNickName'))
    ) {
      tryRenderHeaderInfo();
    } else {
      // 還沒載入就 100ms 後再檢查
      setTimeout(waitHeaderAndRender, 100);
    }
  }

  // DOMContentLoaded 後自動執行
  document.addEventListener('DOMContentLoaded', waitHeaderAndRender);
})();
