const menu = document.querySelector('.menu-toggle');
const sidebar = document.querySelector('.sidebar');
if (menu && sidebar) {
  menu.addEventListener('click', () => {
    const expanded = menu.getAttribute('aria-expanded') === 'true';
    menu.setAttribute('aria-expanded', String(!expanded));
    sidebar.classList.toggle('open', !expanded);
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
      menu.setAttribute('aria-expanded', 'false');
      sidebar.classList.remove('open');
    }
  });
}

const search = document.querySelector('#driver-search');
if (search) {
  search.addEventListener('input', () => {
    const term = search.value.trim().toLocaleLowerCase('es');
    document.querySelectorAll('#drivers-table tbody tr').forEach(row => {
      row.hidden = !row.textContent.toLocaleLowerCase('es').includes(term);
    });
  });
}
