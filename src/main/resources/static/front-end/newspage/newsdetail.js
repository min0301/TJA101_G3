<script>
    document.addEventListener('DOMContentLoaded', () => {
    // Fetch and render news (case-sensitive endpoint)
    console.log('Fetching news...');
    fetch('/api/News/allAll?page=0&size=5')
    .then(r => r.ok ? r.json() : Promise.reject(`Status ${r.status}`))
    .then(async page => {
    console.log('News page:', page);
    const items = await Promise.all(page.content.map(async n => {
    const imgRes = await fetch(`/api/news/image/${n.id}`);
    const imgs = imgRes.ok ? await imgRes.json() : [];
    return {
    ...n,
    cover: imgs[0]?.imgUrl || '/assets/img/placeholder.png'
};
}));
    console.log('News items:', items.length);
    renderNews(items);
})
    .catch(err => console.error('Error fetching news:', err));

    // Render news items
    function renderNews(list) {
    const wrap = document.getElementById('news-list');
    wrap.innerHTML = list.map(n => `
    <div class="post-box d-flex mb-4 align-items-stretch" data-aos="fade-up">
  <div class="news-image-wrapper">
    <img src="${n.cover}" class="news-img rounded-start" alt="news cover">
  </div>
  <div class="card flex-grow-1 shadow-sm rounded-2 border-0 d-flex flex-column">
    <div class="card-body">
      <h3 class="text-teal mb-2">${n.newsTit}</h3>
      <div class="mb-2">
        ${Array.isArray(n.categoryTags)
    ? n.categoryTags.map(tag => `<span class="badge bg-secondary me-1">${tag}</span>`).join('')
    : ''}
      </div>
      <p class="text-muted mb-0">${stripHtml(n.newsCon).slice(0, 100)}&hellip;</p>
    </div>

    <div class="card-footer bg-transparent d-flex justify-content-between align-items-center border-top-0 mt-auto">
      <a href="/front-end/newspage/NewsDetail.html?newsId=${n.id}" class="btn btn-sm btn-mint rounded-pill">閱讀更多</a>
      <div class="text-muted"><i class="bi bi-chat-dots me-1"></i>${n.commentCount || 0}</div>
    </div>

  </div>
</div>

  `).join('');
}

    // Strip HTML tags
    function stripHtml(html) {
    const d = document.createElement('div');
    d.innerHTML = html;
    return d.textContent || d.innerText || '';
}
});
</script>