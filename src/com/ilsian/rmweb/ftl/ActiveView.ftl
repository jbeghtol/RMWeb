<div class="activeheader" id="activeheader">
</div>
<#if rm.permit gte 2>
<!-- GM Code be here! -->

<div class="dropdown">
<button class="rmdeclare" onclick="rollInitiative()">INIT</button><button class="rmexecute" onclick="advanceRound()">ROUND</button>
  <button class="btn btn-default btn-sm dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
    Skills
    <span class="caret"></span>
  </button>
  <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
    <li><a href="#" onclick="requestSkillCheck('alertness')">Alertness</a></li>
    <li><a href="#" onclick="requestSkillCheck('combatawareness')">Combat Awareness</a></li>
    <li><a href="#" onclick="requestSkillCheck('observation')">Observation</a></li>
  </ul>
</div>
</#if>
<div id="divactive">
</div>