<#include "Macros.ftl">
<@pageinit subtitle="Home" context="home" />
<#include "PageBegin.ftl">

<script id="tmplActive" type="text/html">
<table id="activetable" class="table table-dense table-striped">
<thead><tr><th class="rmexecute">Init</th><th>Name</th><th class="rmdeclare">Phase</th><th id="activeskillresult">Result</th><th></th></tr></thead>
<tbody id="activerows"></tbody></table>
</script>

<script id="tmplActiveRow" type="text/html">
<tr>
<td class="rmexecute" data-content="phaseicon" />
<td data-content="name" />
<td class="rmdeclare" data-content="stageselect" title="Snap, Normal, Deliberate" />
<td data-content="result" />
<td class="dropdown">
  <a class="btn btn-xs" data-toggle="dropdown" href="#"><i class="glyphicon glyphicon-tasks"></i></a>
  <ul class="dropdown-menu pull-right" role="menu" data-content-prepend="actionpopup">
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerLoadDefense(this)">Load as Defender</a></li>
    <li class="divider"></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('alertness', this)">Alertness</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('combatawareness', this)">Combat Awareness</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('observation', this)">Observation</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('_', this)">[Custom]</a></li>
  </ul>
</td>
</tr>
</script>

<script id="tmplAllEntities" type="text/html">
<table id="alltable" class="table table-dense table-striped">
<thead><tr><th>Auto</th><th>Name</th><th>Activation</th></tr></thead>
<tbody id="allrows"></tbody></table>
</script>

<script id="tmplAllEntitiesRow" type="text/html">
<tr>
<td data-content="public" /><td data-content="name" />
<td>
<button class="btn btn-xs btn-success" data-template-bind='[{"attribute": "entity", "value": "_id"}]' onclick="changeActivation(this, true)">Load</button>
<button class="btn btn-xs btn-warning" data-template-bind='[{"attribute": "entity", "value": "_id"}]' onclick="changeActivation(this, false)">Remove</button>
</td>
</tr>
</script>

<script language="JavaScript">


var playerts="0";
var logts="0";
var activets="0";
var onlinePlayers = new Array();
var allActives = new Array();

function getInitiativeTitle(round, stage)
{
    switch (stage) {
        case 0: // action decl
            return "Round " + round + ", Actions?";
        case 1: // round time
            return "Round " + round + " In Progress";
        case 2: // post round time
        default:
            return "Round " + round + " Complete";
    }
}

function checkController(clist, name)
{
<#if rm.permit gte 2>
    return true;
</#if>
    var list = clist.split(',');
    for (var i=0; i<list.length; i++) {
        if (name == list[i])
            return true;
    }
    return false;
}

function phaseOption(optdisable, uid, value, optother)
{
    return '<input ' + optdisable + ' ' + optother + ' type="radio" name="initphase' + uid + '" value="' + value + ' " onclick="updateInitiativePhase(' + uid + ', ' + value + ')">';
}

function phaseOptions(uid, optdisable, currval)
{
    var output = '';
    for (var i = -1; i <= 1; i++) {
        var opt = "";
        if (i == currval) opt = "checked";
        output = output + phaseOption(optdisable, uid, i, opt) + '&nbsp;';
    }
    return output; 
}

function makeActionPopup(entity, optdisable)
{
    var all="";
    for (var i=0;i<entity.weapons.length; i++)
    {
        all = all + '<li><a entity="' + entity.uid + '" href="#" onclick="playerLoadAttack(this, ' + i + ')">Att: ' + entity.weapons[i].name + '</a></li>'
    }
    return all;
}

function refreshView()
{
    var syncuri = "gui?action=modelsync&player_ts=" + playerts + "&log_ts=" + logts + "&ent_ts=" + activets;
    console.log("SYNC URI: " + syncuri);
	$.ajax({
           type: "POST",
           url: syncuri,
           success: function(data, textStatus, xhr) {
                if (xhr.status == 204) {
                    // nothing changed in the model, ask again!
                    console.log("Server model is not dirty!");
                    window.setTimeout("refreshView()", 1);
                    return;
                }
               console.log(JSON.stringify(data));
               // player model
               playerts=data.players.mod_ts;
               console.log("Got ModelSync : playerts=" + playerts);
               if (data.players.online) {
                    // update local model
                    onlinePlayers = data.players.online;
                    var inner = "";
                    for (var i=0;i<onlinePlayers.length;i++) {
                        inner += "<li>" + onlinePlayers[i] + "</li>\n";
                    }
                    setElementHtml("ulonline", inner);
               }
               
               // log model
               logts = data.log.mod_ts;
               if (data.log.events) {
                   var currLogs = getElementHtml("rmlog");
                   for (var i=0;i<data.log.events.length; i++) {
                       currLogs += "<div class=\"rmevent\"><div class=\"" + data.log.events[i].type 
                           + "\">" + data.log.events[i].header + "<span style=\"float:right;\"><i class=\"glyphicon glyphicon-user\"></i>&nbsp;" + data.log.events[i].user + "&nbsp;</span></div>" + data.log.events[i].event + "</div>";
                   }
                   setElementHtml("rmlog", currLogs);
               }
                
               // active model
               activets = data.active.mod_ts;
               if (data.active.records)
               {
                    for (var i=0;i<data.active.records.length; i++) {
                        var isAllowed = checkController(data.active.records[i].controllers, '${rm.user}');
                        var optdisable = "";
                        if (!isAllowed)
                            optdisable = "disabled"
    
                        // adds 'disabled' flag for non-controllers, onclick to publish changes
                        data.active.records[i].stageselect = phaseOptions(data.active.records[i].uid, optdisable, data.active.records[i].phase);
                        // labels for init phases
                        switch(data.active.records[i].phase)
                        {
                            case -1:
                                data.active.records[i].phaseicon = data.active.records[i].initiative + ' <i class="glyphicon glyphicon-fire"></i>';
                                break;
                            case 1:
                                data.active.records[i].phaseicon = data.active.records[i].initiative + ' <i class="glyphicon glyphicon-sunglasses"></i>';
                                break;
                            default:
                                data.active.records[i].phaseicon = data.active.records[i].initiative;
                                break;
                        }
                        // button to get a popup of actions
                        data.active.records[i].actionpopup = makeActionPopup(data.active.records[i], optdisable);
                    }
               
                    // apply our pref sort - alphabet for pre, init order after
                    if (data.active.stage > 0) {
                        // sort init
                        data.active.records.sort(function(a, b) { return b.sort - a.sort; });
                    }
                    
                    $('#divactive').loadTemplate( $('#tmplActive'), {} );
                    $('#activerows').loadTemplate( $('#tmplActiveRow'), data.active.records);
                    setElementHtml("activeskillresult", data.active.lastskill);
                    setElementHtml("activeheader", getInitiativeTitle(data.active.round, data.active.stage));
                    $(".rmdeclare").toggle(data.active.stage == 0);
                    $(".rmexecute").toggle(data.active.stage != 0);
                    allActives = data.active.records;
               }
               window.setTimeout("refreshView()", 1);
           },
           error: function(XMLHttpRequest, textStatus, errorThrown) 
           {
           		console.log('Failed to get section data for id ');
                window.setTimeout("refreshView()", 2000);
           }
         });
}

var allEntities = new Array();

function updateEntities()
{
    var enturi = "gui?action=entities";
    console.log("ENT URI: " + enturi);
    $.ajax({
           type: "POST",
           url: enturi,
           success: function(data)
           {
               allEntities = data.entities;
               console.log('Loaded entities total=' + allEntities.length);
               
                $('#rmall').loadTemplate( $('#tmplAllEntities'), {} );
                $('#allrows').loadTemplate( $('#tmplAllEntitiesRow'), allEntities );
           },
           error: function(XMLHttpRequest, textStatus, errorThrown) 
           {
                console.log('Failed to get entity data');
           }
    });
}

function findActiveEntity(uid) {
    for (var i=0; i<allActives.length; i++) {
        if (allActives[i].uid == uid) {
            return allActives[i];
        }
    }
    return null;
}

function rollInitiative()
{
    $.ajax({type: "POST", url: "gui?action=rollinit"});
}

function advanceRound()
{
    $.ajax({type: "POST", url: "gui?action=nextround"});
}

function updateInitiativePhase(uid, phase)
{
    $.ajax({type: "POST", url: "gui?action=setphase&uid=" + uid + "&phase=" + phase });
}

function requestSkillCheck(skillname)
{
    $.ajax({type: "POST", url: "gui?action=skillcheck&skill=" + skillname});
}

function playerSkillCheck(skillname, element)
{
    if (skillname == "_") {
        // custom skill request, ask for the name and value
    } else {
        // canned skill
    }
}

function changeActivation(element, toLoad)
{
    var uid = element.getAttribute('entity');
    if (toLoad)
        $.ajax({type: "POST", url: "gui?action=activate&uid=" + uid});
    else
        $.ajax({type: "POST", url: "gui?action=deactivate&uid=" + uid});
}

function playerLoadAttack(element, weaponindex)
{
    var uid = element.getAttribute('entity');
    var ent = findActiveEntity(uid);
    if (ent) {
        setInputValueById("attacker", ent.name);
        setInputValueById("ob", ent.weapons[weaponindex].ob);
        setSelectOptionById("weaponselect", ent.weapons[weaponindex].uid);
        console.log("Loading attack for: " + ent.name + ", weapon: " + ent.weapons[weaponindex].name);
    }
}

function playerLoadDefense(element)
{
    var uid = element.getAttribute('entity');
    var ent = findActiveEntity(uid);
    if (ent) {
        setInputValueById("defender", ent.name);
        setInputValueById("db", ent.db);
        setSelectOptionById("armorselect", ent.at);
        console.log("Loading defense for uid: " + uid);
        $('#combatview').click();
    }
}

function copyInnerContents(self)
{
	setClipboard(self.innerHTML);
}

function copyNextElement(self) {
	setClipboard(self.nextSibling.innerHTML);
}

function setClipboard(value) {
    var tempInput = document.createElement("input");
    tempInput.style = "position: absolute; left: -1000px; top: -1000px";
    tempInput.value = value;
    document.body.appendChild(tempInput);
    tempInput.select();
    document.execCommand("copy");
    document.body.removeChild(tempInput);
}

function callbackInit() {
    $.addTemplateFormatter({
        RoundPhaseFormatter: function (value, template) {
            var ht = '<input type="radio" name="initphase" value="-1"><input type="radio" name="initphase" value="0"><input type="radio" name="initphase" value="1">';
            return value;
        },
        PlayerMenuIDFormatter: function (value, template) {
            return "PlayerMenu_" + value;
        }
    });
    doRMInit();
    updateEntities();
    refreshView();
}

$(document).ready(callbackInit);
</SCRIPT>

<body class="rmmain">
<div class="rmtop">
<!-- user / logout -->
   <div class="btn-group pull-right">
    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
      <i class="glyphicon glyphicon-user"></i>&nbsp;${rm.user}
      <span class="caret"></span>
    </a>
    <ul class="dropdown-menu">
      <li><a href="/gui?action=logout">Sign Out</a></li>
    </ul>
  </div>
          
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#rmactive">Active</a></li>
<#if rm.permit gte 2>
  <li><a data-toggle="tab" href="#rmall">All</a></li>
</#if>
  <li><a data-toggle="tab" href="#rmonline">Online</a></li>
  <li><a data-toggle="tab" href="#rmcombat" id="combatview">Combat</a></li>
</ul>

<div class="tab-content bgwhite rmheader">
  <div id="rmactive" class="tab-pane in active">
    <#include "ActiveView.ftl">
  </div>
<#if rm.permit gte 2>
  <div id="rmall" class="tab-pane">
  </div>
</#if>
  <div id="rmonline" class="tab-pane">
    <ul id="ulonline" />
  </div>
  <div id="rmcombat" class="tab-pane">
    <#include "CombatDialog.ftl">
  </div>
</div>
</div>
<div class="rmbottom">
<h3>Log</h3>
<div id="rmlog">
</div>
</div>

</div>
</body>
</html>