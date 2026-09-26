(() => {
  const form = document.getElementById('debt-search');
  if (!form) return;
  const byId = id => document.getElementById(id);
  let expanded = form.dataset.additional === 'true';
  const mode = () => form.elements.searchMode.value;
  const show = (id, visible) => {
    const region = byId(id);
    region.hidden = !visible;
    region.querySelectorAll('input, select').forEach(input => { input.disabled = !visible; });
  };
  function render() {
    const current = mode();
    const patient = current === 'dni' || current === 'name';
    show('patient-fields', patient);
    show('dni-fields', current === 'dni');
    show('name-fields', current === 'name');
    byId('dni').required = current === 'dni';
    byId('all-message').hidden = current !== 'all';
    byId('advanced-message').hidden = current !== 'advanced';
    byId('filters-heading').textContent = current === 'advanced' ? '2. Filtros de b\u00fasqueda' : 'Filtros adicionales';
    byId('toggle-filters').hidden = !patient;
    byId('toggle-filters').textContent = expanded ? '\u2212 Ocultar filtros' : '+ Agregar filtros';
    byId('toggle-filters').setAttribute('aria-expanded', String(expanded));
    byId('additional-filters').value = String(patient && expanded);
    show('optional-filters', current === 'advanced' || (patient && expanded));
  }
  form.querySelectorAll('[name="searchMode"]').forEach(radio => radio.addEventListener('change', () => {
    expanded = false;
    byId('search-error').hidden = true;
    render();
  }));
  byId('toggle-filters').addEventListener('click', () => { expanded = !expanded; render(); });
  form.addEventListener('submit', event => {
    let message = '';
    let field;
    if (mode() === 'name' && !byId('nombre').value.trim() && !byId('apellido').value.trim()) {
      message = 'Ingres\u00e1 un nombre, un apellido o ambos.';
      field = byId('nombre');
    } else if (!byId('desde').disabled && byId('desde').value && byId('hasta').value && byId('desde').value > byId('hasta').value) {
      message = 'La fecha desde no puede ser posterior a la fecha hasta.';
      field = byId('desde');
    }
    if (message) {
      event.preventDefault();
      byId('search-error').textContent = message;
      byId('search-error').hidden = false;
      field.focus();
    }
  });
  ['desde', 'hasta', 'tratamiento'].forEach(id => {
    const input = byId(id);
    input.setAttribute('aria-describedby', input.getAttribute('aria-describedby') + ' search-error');
  });
  render();
})();
