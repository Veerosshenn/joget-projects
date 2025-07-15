document.addEventListener('DOMContentLoaded', function () {
    if (window.inlineEditColumnFormatLoaded) return;
    window.inlineEditColumnFormatLoaded = true;

    const contextPath = window.PwaUtil?.contextPath || '/jw';
    const serviceUrl = window.InlineEditColumnFormat?.url || '';
    const tokenName = window.ConnectionManager?.tokenName || '';
    const tokenValue = window.ConnectionManager?.tokenValue || '';
    let translations = {};

    if (serviceUrl) {
        fetch(serviceUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=getTranslations&${tokenName}=${encodeURIComponent(tokenValue)}`
        }).then(r => r.json()).then(data => translations = data).catch(e => console.warn('Translation load failed:', e));
    }

    function sanitizeInput(value, fieldType) {
        if (['number', 'date', 'dateTime', 'timeOnly'].includes(fieldType)) return value;
        return String(value || '').replace(/[<>&"'\\]/g, c => ({
            '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;', "'": '\\\'', '\\': '\\\\'
        })[c] || c);
    }

    const validationRules = {
        textfield: {
            regex: /^[a-zA-Z0-9\s.,!?'-]*$/,
            message: 'Invalid input: only alphanumeric characters, spaces, and common punctuation are allowed.'
        },
        textarea: {
            regex: /^[a-zA-Z0-9\s.,!?'\n-]*$/,
            message: 'Invalid input: only alphanumeric characters, spaces, newlines, and common punctuation are allowed.'
        },
        select: {
            validate: (value, options) => {
                try {
                    const opts = JSON.parse(options || '[]');
                    return Array.isArray(opts) && opts.some(opt => opt.value === value);
                } catch { return false; }
            },
            message: 'Invalid selection: please choose a valid option.'
        },
        number: {
            validate: value => !isNaN(parseFloat(value)) && isFinite(value),
            message: 'Invalid number.'
        }
    };

    function initializeDatepicker(input, type, format, isBE, save) {
        const opts = {
            datePickerType: type,
            showOn: 'focus',
            isBE,
            changeMonth: true,
            changeYear: true,
        };
        if (type === 'date') {
            opts.dateFormat = format.replace('yyyy', 'yy').replace('MM', 'mm');
            opts.onSelect = val => { input.value = val; save(); };
        } else if (type === 'dateTime') {
            const parts = format.split(' ');
            const df = parts[0] || 'yy-mm-dd';
            const tf = parts.slice(1).join(' ') || 'hh:mm TT';
            opts.dateFormat = df.replace('yyyy', 'yy').replace('MM', 'mm');
            opts.timeFormat = tf;
            opts.controlType = 'select';
            opts.stepHour = 1;
            opts.stepMinute = 1;
            opts.ampm = /tt/i.test(tf);
            opts.onClose = () => {
                save();
                // Explicitly hide and destroy the datepicker
                try {
                    $(input).cdatepicker('hide');
                    $(input).cdatepicker('destroy');
                } catch (e) {
                    console.warn('[InlineEdit] Failed to hide/destroy datepicker in onClose:', e);
                }
            };
        } else if (type === 'timeOnly') {
            opts.timeFormat = format || 'HH:mm';
            opts.controlType = 'select';
            opts.stepHour = 1;
            opts.stepMinute = 1;
            opts.onClose = () => {
                save();
                // Explicitly hide and destroy the datepicker
                try {
                    $(input).cdatepicker('hide');
                    $(input).cdatepicker('destroy');
                } catch (e) {
                    console.warn('[InlineEdit] Failed to hide/destroy datepicker in onClose:', e);
                }
            };
        }

        try {
            $(input).cdatepicker(opts);
            setTimeout(() => {
                if (type === 'date') $(input).datepicker('show');
                else if (type === 'dateTime' && typeof $(input).datetimepicker === 'function') $(input).datetimepicker('show');
                else if (type === 'timeOnly' && typeof $(input).timepicker === 'function') $(input).timepicker('show');
                else { console.warn('[InlineEdit] Picker fallback'); $(input).datepicker('show'); }
            }, 100);
        } catch (e) { console.error('[InlineEdit] Failed to initialize picker:', e); }
    }

    function ensureDependencies(callback) {
        if (typeof $.fn.datepicker !== 'function') {
            const s1 = document.createElement('script');
            s1.src = contextPath + '/js/jquery/ui/jquery-ui.min.js';
            s1.onload = () => checkCdatepicker(callback);
            document.head.appendChild(s1);
        } else checkCdatepicker(callback);
    }

    function checkCdatepicker(callback) {
        if (typeof $.fn.cdatepicker !== 'function') {
            const s2 = document.createElement('script');
            s2.src = contextPath + '/plugin/org.joget.plugin.datalist.InlineEditColumnFormat/js/jquery.custom.datepicker.js';
            s2.onload = () => checkTimepickerAddon(callback);
            document.head.appendChild(s2);
        } else checkTimepickerAddon(callback);
    }

    function checkTimepickerAddon(callback) {
        if (typeof $.fn.datetimepicker !== 'function' || typeof $.fn.timepicker !== 'function') {
            const css = document.createElement('link');
            css.rel = 'stylesheet';
            css.href = contextPath + '/plugin/org.joget.plugin.datalist.InlineEditColumnFormat/css/jquery-ui-timepicker-addon.css';
            document.head.appendChild(css);

            const s3 = document.createElement('script');
            s3.src = contextPath + '/plugin/org.joget.plugin.datalist.InlineEditColumnFormat/js/jquery-ui-timepicker-addon.js';
            s3.onload = callback;
            document.head.appendChild(s3);
        } else callback();
    }

    ensureDependencies(() => {
        // Track the currently active input and its save function
        let activeInput = null;
        let activeSave = null;

        // Handle clicks outside the editing cell and datepicker
        function handleOutsideClick(event) {
            if (!activeInput) return;

            // Allow clicks inside the input, the datepicker widget, or inline edit cells
            const picker = document.getElementById('ui-datepicker-div');
            if (
                activeInput.contains(event.target) ||
                (picker && picker.contains(event.target)) ||
                event.target.closest('.inline-edit-cell') ||
                event.target.closest('.ui-datepicker-header') ||
                event.target.closest('.ui-datepicker-prev') ||
                event.target.closest('.ui-datepicker-next') ||
                event.target.closest('.ui-datepicker-title') ||
                event.target.closest('.ui-datepicker-buttonpane') ||
                event.target.closest('.ui-datepicker-current') ||
                event.target.closest('.ui-datepicker-close')
            ) {
                return;
            }

            // Otherwise, clicked outside > save & clear active input
            activeSave && activeSave();
            activeInput = null;
            activeSave = null;
        }

        document.querySelectorAll('.inline-edit-cell').forEach(cell => {
            cell.addEventListener('click', function (e) {
                if (cell.querySelector('input, select, textarea, .inline-edit-error')) return;

                // If another cell is being edited, save it before starting new edit
                if (activeInput && activeSave) {
                    activeSave();
                }

                const rowId = cell.dataset.rowId || '';
                const columnName = cell.dataset.columnName || '';
                const fieldType = cell.dataset.fieldType || 'textfield';
                const datePickerType = cell.dataset.datePickerType || 'date';
                const format = cell.dataset.format || 'yyyy-MM-dd';
                const isBE = cell.dataset.isBe === 'true';
                const options = cell.dataset.options || '[]';
                const originalValue = cell.textContent.trim();
                const formFieldId = columnName.startsWith('c_') ? columnName : 'c_' + columnName;

                let input;
                function save() {
                    let newValue = input.value || '';
                    if (['textfield', 'textarea'].includes(fieldType)) {
                        newValue = sanitizeInput(newValue, fieldType);
                    }
                    const validationKey = ['date', 'dateTime', 'timeOnly'].includes(fieldType) ? null : fieldType;
                    const rule = validationRules[validationKey];
                    if (rule && ((rule.regex && !rule.regex.test(newValue)) || (rule.validate && !rule.validate(newValue, options, format)))) {
                        alert(rule.message || 'Invalid input');
                        cell.textContent = originalValue;
                        // Clean up active input
                        activeInput = null;
                        activeSave = null;
                        return;
                    }

                    cell.innerHTML = '';
                    cell.textContent = newValue;
                    cell.classList.add('inline-edit-saving');

                    fetch(serviceUrl, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: `action=save&rowId=${encodeURIComponent(rowId)}&columnName=${encodeURIComponent(formFieldId)}&value=${encodeURIComponent(newValue)}&${tokenName}=${encodeURIComponent(tokenValue)}`
                    }).then(r => r.json()).then(data => {
                        cell.classList.remove('inline-edit-saving');
                        if (data.status !== 'success') {
                            alert(data.message || 'Save failed');
                            cell.textContent = originalValue;
                        } else {
                            cell.textContent = newValue;
                        }
                        // Clean up active input
                        activeInput = null;
                        activeSave = null;
                    }).catch(() => {
                        cell.classList.remove('inline-edit-saving');
                        alert('Network error. Data will be queued.');
                        cell.textContent = newValue;
                        // Clean up active input
                        activeInput = null;
                        activeSave = null;
                    });
                }

                if (fieldType === 'date' || fieldType === 'dateTime' || fieldType === 'timeOnly') {
                    input = document.createElement('input');
                    input.type = 'text';
                    input.className = 'inline-edit-date';
                    input.value = originalValue;
                    cell.innerHTML = '';
                    cell.appendChild(input);
                    initializeDatepicker(input, datePickerType, format, isBE, save);
                } else if (fieldType === 'textfield') {
                    input = document.createElement('input');
                    input.type = 'text';
                    input.value = originalValue;
                    input.className = 'inline-edit-input';
                } else if (fieldType === 'textarea') {
                    input = document.createElement('textarea');
                    input.value = originalValue;
                    input.className = 'inline-edit-textarea';
                } else if (fieldType === 'select') {
                    input = document.createElement('select');
                    input.className = 'inline-edit-select';
                    try {
                        const opts = JSON.parse(options);
                        if (!Array.isArray(opts)) throw '';
                        opts.forEach(opt => {
                            const o = document.createElement('option');
                            o.value = opt.value;
                            o.text = opt.label || opt.value;
                            if (opt.value === originalValue) o.selected = true;
                            input.appendChild(o);
                        });
                    } catch {
                        input = document.createElement('span');
                        input.textContent = 'Invalid options';
                        input.className = 'inline-edit-error';
                    }
                } else if (fieldType === 'number') {
                    input = document.createElement('input');
                    input.type = 'number';
                    input.value = originalValue;
                    input.className = 'inline-edit-number';
                } else {
                    input = document.createElement('span');
                    input.textContent = 'Unsupported field type';
                    input.className = 'inline-edit-error';
                }

                if (!cell.contains(input)) {
                    cell.innerHTML = '';
                    cell.appendChild(input);
                }
                input.focus();

                // Set the active input and save function
                activeInput = input;
                activeSave = save;

                input.addEventListener('keydown', e => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        save();
                    } else if (e.key === 'Escape') {
                        e.stopImmediatePropagation();
                        $('#ui-datepicker-div').hide();
                        cell.textContent = originalValue;
                        activeInput = null;
                        activeSave = null;
                    }
                });
            });
        });
        // Add document-level click listener for outside clicks
        document.addEventListener('click', handleOutsideClick);
    });
});
