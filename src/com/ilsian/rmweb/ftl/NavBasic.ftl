<nav class="navbar navbar-default navbar-fixed-top">
  <div class="container-fluid">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <!-- Note: Removed top padding to keep graphic cleanly within the Nav Bar -->
      <a class="navbar-brand" style="padding-top: 0px !important;" href="gui"><img alt="RM Lookup" src="/res/nav_title.png"></a>
    </div>
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
		<div class="btn-group pull-right">
            <a class="btn" href="/gui">
                <#if rm.hideback??>
		        &nbsp;
    		    <#else>
                Back
                </#if>
            </a>
        </div>
    </div>
  </div>
</nav>

<#---------------------------------------------------------------------
 SIGNALS - These HTML elements have ID's which are used to signal
 changes to elements that can be impacted by BG actions.  JS actions
 that change reports should fire the change event on sigReports so
 pages that require updates can reload them.
 
 -- Example -- reload page when reports have changed
	$('#sigReports').change(function() {
		location.reload();
	});
 ---------------------------------------------------------------------->
<div id="signals" style="display: none;">
<span id="sigConfig"></span><span id="sigTasks"></span>
</div>

<div id="vabgheader" style="display:none">
</div>