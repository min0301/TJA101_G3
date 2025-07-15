fetch('/api/news/image/index')
    .then(res => res.json())
    .then(data => {
        const inner = document.querySelector('#newsCarousel .carousel-inner');
        const modalContainer = document.getElementById('newsLightboxes');

        data.forEach((n, idx) => {
            const item = document.createElement('div');
            item.className = 'carousel-item' + (idx === 0 ? ' active' : '');

            item.innerHTML = `
        <a data-bs-toggle="modal" data-bs-target="#lightbox-${n.newsNoId}">
          <img src="${n.imgUrl}" class="d-block w-100" alt="${n.newsNoNewsTit}">
        </a>
        <div class="carousel-caption">
          <a href="/front-end/newspage/NewsDetail.html?newsId=${n.newsNoId}" class="stretched-link text-white">${n.newsNoNewsTit}</a>
        </div>
      `;
            inner.appendChild(item);

            const modal = document.createElement('div');
            modal.className = 'modal fade';
            modal.id = `lightbox-${n.newsNoId}`;
            modal.tabIndex = -1;
            modal.setAttribute('aria-hidden', 'true');

            modal.innerHTML = `
        <div class="modal-dialog modal-dialog-centered modal-xl">
          <div class="modal-content bg-transparent border-0">
            <img src="${n.imgUrl}" class="w-100 rounded-3" alt="${n.newsNoNewsTit}">
          </div>
        </div>
      `;
            modalContainer.appendChild(modal);
        });
    })
    .catch(err => {
        console.error("載入首頁輪播失敗：", err);
    });
