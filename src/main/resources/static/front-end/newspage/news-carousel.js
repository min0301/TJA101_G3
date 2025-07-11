// === 0. 你的新聞資料（未來可由 Ajax / fetch 取回） =============
const newsData = [
    {
        id: 1,
        title: '生存策略遊戲《惡靈古堡 生存兵種》開放事前登錄　預計於 2025 年內推出',
        img: '/images/news_img/new1-1.png',
        link: '/news/1'
    },
    {
        id: 2,
        title: 'Steam 夏季特賣開跑！錢包君表示：我還沒準備好啊！',
        img: '/images/news_img/new2-1.png',
        link: '/news/2'
    },
    {
        id: 3,
        title: '台灣獨立遊戲《時空紙牌》闖入全球暢銷榜 Top 10 創下新紀錄',
        img: '/images/news_img/new3-1.png',
        link: '/news/3'
    }
];

// === 1. 產生 Carousel Slide ======================================
const inner = document.querySelector('#newsCarousel .carousel-inner');
const modalContainer = document.getElementById('newsLightboxes');

newsData.forEach((n, idx) => {
    /* 1-1 生成輪播項目 ---------------------------------------- */
    const item = document.createElement('div');
    item.className = 'carousel-item' + (idx === 0 ? ' active' : '');

    item.innerHTML = `
    <a data-bs-toggle="modal" data-bs-target="#lightbox-${n.id}">
      <img src="${n.img}" class="d-block w-100" alt="${n.title}">
    </a>
    <div class="carousel-caption">
      <a href="${n.link}" class="stretched-link text-white">${n.title}</a>
    </div>
  `;
    inner.appendChild(item);

    /* 1-2 生成對應 Modal（燈箱） ------------------------------ */
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.id = `lightbox-${n.id}`;
    modal.tabIndex = -1;
    modal.setAttribute('aria-hidden', 'true');

    modal.innerHTML = `
    <div class="modal-dialog modal-dialog-centered modal-xl">
      <div class="modal-content bg-transparent border-0">
        <img src="${n.img}" class="w-100 rounded-3" alt="${n.title}">
      </div>
    </div>
  `;
    modalContainer.appendChild(modal);
});
