<div id="floatnote" style="display: none; position: absolute;top: 0px;left:5px;z-index:9999;"></div>
<nav class="navbar navbar-default navbar-fixed-top">
  <div class="container-fluid">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
      <!-- Note: Removed top padding to keep graphic cleanly within the Nav Bar -->
      <a class="navbar-brand" style="padding-top: 0px !important;" href="gui"><img alt="${apptitle}" src="/res/nav_title.png"></a>
<!-- MAIN MENU: START -->            
            <div class="btn-group pull-left">
            <a class="btn btn-info lhmenu dropdown-toggle" data-toggle="dropdown" href="#">
              Menu
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu">
              <li class="dropdown-submenu">
              	<a href="#">Something</a>
              	<ul class="dropdown-menu scrollable-menu" role="menu">
	              	<li><a href="#" onclick="return coming_soon(this)" report_uid="-1" >Do Something</a></li>
              	</ul>
              </li>
              <li><a href="#" onclick="return do_simple_combat(this)" >Combat</a></li>
              <li class="divider"></li>
              <li><a href="gui?ftl=ManageStuff" >Manage Stuff</a></li>
              <li class="divider"></li>
              <li><a href="#" onclick="lhem_file_upload_dialog('update', 'sigUpdates')" >Upload Something</a></li>
              <#if rm.permit gte 2>
            	<#include "GMDataMenu.ftl">
              </#if>
              <#if rm.permit == 3>
            	<#include "AdminNavMenu.ftl">
              </#if>
              <li class="divider"></li>
              <li><a href="gui?ftl=AboutPage.ftl" >About</a></li>
            </ul>
            </div>
<!-- MAIN MENU: END -->      
           <div class="btn-group pull-right">
            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
              <i class="glyphicon glyphicon-user"></i>&nbsp;${rm.user}
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu">
              <li><a href="/gui?action=logout">Sign Out</a></li>
            </ul>
          </div>
    </div>
  </div><!-- /.container-fluid -->
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