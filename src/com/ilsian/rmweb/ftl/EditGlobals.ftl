<#include "Macros.ftl">
<@pageinit subtitle="Settings" />
<#include "PageBegin.ftl">
<body>

<div class="well">
<label class="contol-label" >Moxie Search</label>
<div class="form-group">
    <input type="text" size="200" classs="form-control" id="search" name="search" placeholder="Enter email, name, or serial number." ><button>Search</button>
</div>
</div>
<table id="activetable" class="table table-dense table-striped nomargin">
<tr><th>User</th><th>Email</th><th>Robots</th><th>Status</th></tr>
<tr><td>Justin Beghtol</td><td>duranaki@gmail.com</td><td>200121231912</td><td>Paired</td></tr>
</table>












<#if rm.permit gte 9>
<#include "NavBasic.ftl">
<br />
<br />
<div class="well">
<h3>Global Settings</h3>
<form id="rmwebconfig" action="gui?ftl=${ftlname}" method="POST">
<@settingblock_bool name="Combat Tracker" formname="combat_tracker" value=combat_tracker />
<@settingblock_bool name="Tracker Requires Accept" formname="affirmative_tracker" value=affirmative_tracker />
<@settingblock_bool name="Condition Mods" formname="condition_mods" value=condition_mods />
<@settingblock_commit />
</div>
</#if>
</body>
</html>