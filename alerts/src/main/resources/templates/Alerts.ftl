<div class="my-3">
    <div class="alert alert-${alertType!} d-flex align-items-start"
         role="alert" aria-live="assertive" aria-atomic="true">

        <#if icon?? && icon?length gt 0>
            <i class="${icon} me-2 mt-1" aria-hidden="true"></i>
        </#if>

        <div class="flex-grow-1">
            <#if title?? && title?length gt 0>
                <h5 class="alert-heading mb-1">${title}</h5>
            </#if>

            <#if message?? && message?length gt 0>
                <p class="mb-1">${message}</p>
            </#if>

            <#if customContent?? && customContent?length gt 0>
                <div class="mt-2">${customContent}</div>
            </#if>
        </div>
    </div>
</div>
