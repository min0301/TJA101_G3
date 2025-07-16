document.addEventListener('DOMContentLoaded', () => {
  const menuButtons = document.querySelectorAll('#menu button');

  const pageMap = {
    order: '/order.html',
    coupon: '/coupon.html',
    product: '/product.html',
    favorite: '/favorite.html'
  };

  menuButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const key = btn.dataset.link;
      if (pageMap[key]) {
        window.location.href = pageMap[key];
      } else {
        console.warn(`無對應頁面：${key}`);
      }
    });
  });
});
