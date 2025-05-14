<div class="form-cell" ${elementMetaData!}>
    <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.tutorial.ColorPicker/js/colorPick.min.js"></script>
     <link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.tutorial.ColorPicker/css/colorPick.min.css">
    <script type="text/javascript">
      $(function(){
          initialColor = $("#${elementParamName!}").val();
          console.log("initial " + initialColor);
          $("#${elementParamName!}").colorPick({ 
            'initialColor' : initialColor,
          'onColorSelected': function() {
            //console.log("The user has selected the color: " + this.color)
            $("#${elementParamName!}").val(this.color);
            this.element.css({'backgroundColor': this.color, 'color': this.color});
          } 
        });
      });
    </script>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value"><span>${value!?html}</span></div>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" class="colorPicker" value="${value!?html}" />
    <#else>
        <input id="${elementParamName!}" name="${elementParamName!}" type="text" class="colorPicker" size="${element.properties.size!}" value="${value!?html}" maxlength="${element.properties.maxlength!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> />
    </#if>
</div>