async function renderForumTable() {
    const tbody = document.getElementById('forum-data-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    try {
        const res = await fetch('/api/forums');
        const forumList = await res.json();
        forumList.forEach(forum => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
<!--                <td>${forum.id}</td>-->
                <td>${forum.forName}</td>
                <td>${forum.categoryName || ''}</td>
<!--                <td>${forum.forImgUrl ? `<img src="${forum.forImgUrl}" alt="論壇圖" style="max-width:60px;">` : ''}</td>-->
                <td>${forum.forDes || ''}</td>
<!--                <td>${forum.forDate ? new Date(forum.forDate).toLocaleString() : ''}</td>-->
                <td>${forum.forUpdate ? new Date(forum.forUpdate).toLocaleString() : ''}</td>
<!--                <td>${forum.forStatus === '0' ? '正常' : '停用'}</td>-->
                <td>
                    <button class="forum-btn">進入</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="9">資料載入失敗</td></tr>';
    }
}
renderForumTable();