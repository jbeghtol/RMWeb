<#include "Macros.ftl">
<@pageinit subtitle="Permission Fail" />
<#include "PageBegin.ftl">
<body>
<#include "NavBasic.ftl">

<div class="well alert alert-danger">
<h4><i class="glyphicon glyphicon-alert"></i>&nbsp;Access Denied</h4>
<p>You do not have sufficient permissions to access this resource.</p>
<#if urlparams??>
  <#if urlparams.need??>
      <p>Required Level: <strong>${urlparams.need[0]}</strong></p>
  </#if>
  <#if urlparams.has??>
      <p>Your Level: <strong>${urlparams.has[0]}</strong></p>
  </#if>
</#if>
</div>

</body>
</html>
