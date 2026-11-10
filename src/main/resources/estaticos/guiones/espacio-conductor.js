const current = location.pathname;
const sidebar = document.getElementById('workspace-navigation');
const toggle = document.getElementById('menu-toggle');
const close = document.getElementById('menu-close');
const backdrop = document.getElementById('menu-backdrop');
const mobile = matchMedia('(max-width: 900px)');
for (const link of sidebar.querySelectorAll('.nav-links a')) {
  const target = new URL(link.href).pathname;
  if (current === target || current.startsWith(target + '/') ||
      (target === '/ui/documents' && /^\/ui\/drivers\/[^/]+\/documents/.test(current))) {
    link.setAttribute('aria-current', 'page');
  }
}
function setOpen(open, restoreFocus = false) {
  const expanded = mobile.matches && open;
  document.body.classList.toggle('menu-open', expanded);
  toggle.setAttribute('aria-expanded', String(expanded));
  sidebar.inert = mobile.matches && !expanded;
  backdrop.hidden = !expanded;
  if (expanded) close.focus();
  else if (restoreFocus) toggle.focus();
}
toggle.addEventListener('click', () => setOpen(toggle.getAttribute('aria-expanded') !== 'true'));
close.addEventListener('click', () => setOpen(false, true));
backdrop.addEventListener('click', () => setOpen(false, true));
sidebar.addEventListener('keydown', event => {
  if (!mobile.matches || toggle.getAttribute('aria-expanded') !== 'true') return;
  if (event.key === 'Escape') { event.preventDefault(); setOpen(false, true); }
  if (event.key === 'Tab') {
    const items = [...sidebar.querySelectorAll('a,button')].filter(item => item.offsetParent !== null);
    const first = items[0], last = items[items.length - 1];
    if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
    else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
  }
});
mobile.addEventListener('change', () => setOpen(false));
setOpen(false);

for (const form of document.querySelectorAll('[data-document-review-form]')) {
  const decision = form.querySelector('[data-document-review-decision]');
  const reason = form.querySelector('[data-document-review-reason]');
  const reasonContainer = form.querySelector('[data-document-review-reason-container]');
  const label = form.querySelector('[data-document-review-reason-label]');
  const hint = form.querySelector('[data-document-review-reason-hint]');
  const updateReasonRequirement = () => {
    const required = decision.value !== 'APPROVED';
    reasonContainer.hidden = !required;
    reason.required = required;
    reason.setAttribute('aria-required', String(required));
    label.textContent = required ? 'Motivo de la decisión *' : 'Motivo de la decisión (opcional)';
    hint.textContent = required
      ? 'Obligatorio para observar o rechazar un documento.'
      : 'Opcional al aprobar.';
  };
  decision.addEventListener('change', updateReasonRequirement);
  updateReasonRequirement();
}
