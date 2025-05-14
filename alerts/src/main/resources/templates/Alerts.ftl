<div>
    <div class="alert alert-${alertType!} d-flex align-items-start p-0 m-0" role="alert" aria-live="assertive" aria-atomic="true">
        <#if icon?? && icon?length gt 0>
            <i class="${icon}" aria-hidden="true"></i>
        </#if>
        <div class="flex-grow-1">
            <#if title?? && title?length gt 0>
                <h5 class="alert-heading">${title}</h5>
            </#if>
            <#if message?? && message?length gt 0>
                <p>${message}</p>
            </#if>
            <#if customContent?? && customContent?length gt 0>
                <div>${customContent}</div>
            </#if>
        </div>
    </div>
</div>
