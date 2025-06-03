<div class="form-cell" ${elementMetaData!} data-custom-palette="${element.properties.customPalette!}">
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
        <div class="form-cell-value"><span>${value!?html}</span></div>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" class="coloris_${elementParamName!}" value="${value!?html}" />
    <#else>
        <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
        <div class="colorPicks">
            <div class="colorPick">
                <input id="${elementParamName!}" name="${elementParamName!}" type="text" class="coloris_${elementParamName!}" size="${element.properties.size!}" value="${value!?html}" maxlength="${element.properties.maxlength!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> />
            </div>
        </div>
    </#if>

    <#if element.properties.readonly! != 'true'>
    <#if !(request.getAttribute("org.joget.marketplace.ColorPicker_EDITABLE")??)>
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.marketplace.ColorPicker/js/colorPicker.min.js"></script>
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.marketplace.ColorPicker/js/customColorPicker.js"></script>
    <link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.marketplace.ColorPicker/css/colorPicker.min.css">
    <link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.marketplace.ColorPicker/css/customColorPicker.css">
    </#if>

        <script type="text/javascript">
            $(function() {
                Coloris({
                    el: '.coloris_${elementParamName!}',
                });

                const customPalette = $('div.form-cell[data-custom-palette]').data('customPalette');
                const swatches = customPalette
                    ? customPalette.split(',').map(color => color.trim()).filter(color => /^#[0-9A-F]{6}$/i.test(color))
                    : [
                        '#264653', '#2a9d8f', '#e9c46a', '#f4a261', '#e76f51', '#d62828'
                    ];

                // Set Coloris instance with custom or default swatches
                Coloris.setInstance('.coloris_${elementParamName!}', {
                    theme: '${element.properties.colorisTheme!}',
                    themeMode: '${element.properties.themeMode!}',
                    formatToggle: true,
                    alpha: true,
                    clearButton: true,
                    saveButton: true,
                    closeButton: false,
                    swatchesOnly: false,
                    swatches: swatches
                });
            });
        </script>
    </#if>
</div>