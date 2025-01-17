<#include "Macros.ftl">
<@pageinit subtitle="Settings" />
<#include "PageBegin.ftl">
<body>
<#include "NavBasic.ftl">
<br />
<br />
<div class="well">
<h3>Global Settings</h3>
<form id="rmwebconfig" action="gui?ftl=${ftlname}" method="POST">
<@settingblock_bool name="Allow Unknown Players" formname="allow_unknown" value=allow_unknown />
<@settingblock_bool name="Combat Tracker" formname="combat_tracker" value=combat_tracker />
<@settingblock_bool name="Tracker Requires Accept" formname="affirmative_tracker" value=affirmative_tracker />
<@settingblock_bool name="Condition Mods" formname="condition_mods" value=condition_mods />
<@settingblock_string name="Entity Links" formname="entity_links" value=entity_links />
<@settingblock_commit />
</div>
</body>
</html>