<style>
    body [data-cbuilder-invisible][data-cbuilder-label]::after {
        padding-top: 0 !important;
    }
</style>

<div class="alert alert-${alertType!} d-flex align-items-start p-0 m-0 w-100 position-relative"
     role="alert"
     aria-live="assertive"
     aria-atomic="true"
        <#if !(title?? && title?length gt 0) && !(message?? && message?length gt 0) && !(customContent?? && customContent?length gt 0)>
            data-cbuilder-invisible data-cbuilder-label="Alerts"
        </#if>
>
    <#if icon?? && icon?length gt 0>
        <i class="${icon}" aria-hidden="true" style="margin: 0; padding: 0;"></i>
    </#if>

    <div class="flex-grow-1" style="margin: 0; padding: 0;">
        <#if title?? && title?length gt 0>
            <h5 class="alert-heading m-0 p-0">${title}</h5>
        </#if>
        <#if message?? && message?length gt 0>
            <p class="m-0 p-0">${message}</p>
        </#if>
        <#if customContent?? && customContent?length gt 0>
            <div class="m-0 p-0">${customContent}</div>
        </#if>
    </div>
</div>
