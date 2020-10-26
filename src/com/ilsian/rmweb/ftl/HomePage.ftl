<#include "Macros.ftl">
<@pageinit subtitle="Home" context="home" />
<#include "PageBegin.ftl">

<script id="tmplActive" type="text/html">
<table id="activetable" class="table table-dense table-striped nomargin">
<colgroup>
    <col width="10%">
    <col width="10%">
    <col width="30%">
    <col width="20%">
    <col width="30%">
</colgroup>
<thead><tr><th class="rmexecute">Init</th><th class="rmdeclare">Phase</th><th>Name</th><th class="rmresult" id="activeskillresult">Result</th><th></th></tr></thead>
<tbody id="activerows"></tbody></table>
</script>

<script id="tmplActiveRow" type="text/html">
<tr>
<td class="rmexecute" data-template-bind='[{"attribute": "title", "value": "initexplain"}]' data-content="phaseicon" />
<td class="rmdeclare" data-content="stageselect" title="Snap, Normal, Deliberate" />
<td data-content-prepend="name" data-template-bind='[{"attribute": "title", "value": "effects.detail"}]' ><span data-content="effects.brief" /></td> 
<td data-template-bind='[{"attribute": "title", "value": "explain"}]' class="rmresult" data-content="result" />
<td class="dropdown">
<#if rm.permit gte 3>
  <a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" class="btn btn-xs" onclick="toggleEntityVisibility(this)">
  <i data-template-bind='[{"attribute": "class", "value": "visibility", "formatter": "VisibilityFormatter"}]'></i>
  </a>
  <a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" class="btn btn-xs" onclick="changeActivation(this, false)">
  <i class="glyphicon glyphicon-remove red"></i>
  </a>
</#if>
  <a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" class="btn btn-xs" onclick="playerSkillCheck('-', this)"><i class="glyphicon glyphicon-certificate"></i></a>
  <a class="btn btn-xs" data-toggle="dropdown" href="#"><i class="glyphicon glyphicon-tasks"></i></a>
  <ul class="dropdown-menu pull-left" role="menu" data-content-prepend="actionpopup">
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerLoadDefense(this)">Load as Defender</a></li>
    <li class="divider"></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('alertness', this)">Alertness</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('combatawareness', this)">Combat Awareness</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('observation', this)">Observation</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('powerperception', this)">Power Perc.</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('breakstun', this)">Break Stun</a></li>
    <li><a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" onclick="playerSkillCheck('_', this)">[Custom]</a></li>
  </ul>
</td>
</tr>
</script>

<script id="tmplAllEntities" type="text/html">
<table id="alltable" class="table table-dense table-striped nomargin">
<thead><tr><th>Auto</th><th>Name</th><th>Load</th><th>Remove&nbsp;
<button class="btn btn-primary btn-xs" title="Upload updates to entities" onclick="rm_file_upload_dialog('sigEntities')">Import <i class="glyphicon glyphicon-floppy-open"></i></button>
</th></tr></thead>
<tbody id="allrows"></tbody></table>
</script>

<script id="tmplAllEntitiesRow" type="text/html">
<tr>
<td data-content="public" /><td data-content="name" />
<td>
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"}]' title="Show in Active" onclick="changeActivation(this, true, false)"><i class="glyphicon glyphicon-eye-open"></i></a>
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"}]' title="Hide in Active" onclick="changeActivation(this, true, true)"><i class="glyphicon glyphicon-eye-close"></i></a>
</td>
<td>
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"}]' title="Remove from Active" onclick="changeActivation(this, false)"><i class="glyphicon glyphicon-log-out"></i></a>
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"},{"attribute": "entname", "value": "name"}]' title="Delete from Database" onclick="deleteEntity(this)"><i class="glyphicon glyphicon-trash" style="color: red;"></i></a>
</td>
</tr>
</script>

<script id="tmplAllEntitiesGroup" type="text/html">
<tr class="groupheader">
<td colspan="2" class="groupheader" data-content="tag" />
<td class="groupheader">
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"}]' title="Show in Active" onclick="changeGroupActivation(this, true, false)"><i class="glyphicon glyphicon-eye-open"></i></a>
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"}]' title="Hide in Active" onclick="changeGroupActivation(this, true, true)"><i class="glyphicon glyphicon-eye-close"></i></a>
</td>
<td class="groupheader">
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"}]' title="Remove from Active" onclick="changeGroupActivation(this, false)"><i class="glyphicon glyphicon-log-out"></i></a>
<a class="btn btn-xs" data-template-bind='[{"attribute": "entity", "value": "_id"},{"attribute": "enttag", "value": "tag"}]' title="Delete from Database" onclick="deleteEntityGroup(this)"><i class="glyphicon glyphicon-trash" style="color: red;"></i></a>
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
                   if (data.log.events.length>0) {
                       if (data.log.events[0].event == "System reset") {
                           setElementHtml("rmlog", "");
                       }
                   }
                   for (var i=0;i<data.log.events.length; i++) {
                       var logline = "<div class=\"rmevent\"><div class=\"" + data.log.events[i].type 
                           + "\">" + data.log.events[i].header + "<span style=\"float:right;\"><i class=\"glyphicon glyphicon-user\"></i>&nbsp;" + data.log.events[i].user + "&nbsp;</span></div><div class=\"eventresultbox\">" + data.log.events[i].event + "</div></div>";
                       $( "#rmlog" ).append(logline);
                   }
                   var d = $('#rmlog');
                   d.scrollTop(d.prop("scrollHeight"));
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
               
                $('#rmallcontents').loadTemplate( $('#tmplAllEntities'), {} );
                
                // split into groups
                var groupName = '_';
                var groupList;
                var groupLists = new Array();
                for (var i=0;i<allEntities.length; i++) {
                    if (groupName != allEntities[i].tag) {
                        groupName = allEntities[i].tag;
                        groupList = new Array();
                        groupLists.push(groupList);                        
                    }
                    groupList.push(allEntities[i]);
                }
                
                for (var i=0;i<groupLists.length;i++) {
                    groupList = groupLists[i];
                    if (i==0) {
                        $('#allrows').loadTemplate( $('#tmplAllEntitiesGroup'), groupList[0] );
                    } else {
                        $('#allrows').loadTemplate( $('#tmplAllEntitiesGroup'), groupList[0], { append: true } );
                    }    
                    $('#allrows').loadTemplate( $('#tmplAllEntitiesRow'), groupList, { append: true } );
                }
                //$('#allrows').loadTemplate( $('#tmplAllEntitiesRow'), allEntities );
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

function request_archive()
{
    rm_confirm_dialog("Confirm Clean", "Reset to round 1 and clear log?", function() {
       $.ajax({type: "POST", url: "gui?action=cleanslate"})
    });
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
    var uid = element.getAttribute('entity');
    if (skillname == "_") {
        // custom skill request, ask for the name and value
        prompt_custom_skill( function (skname, skval) {
            console.log("Rolling Skill: " + skname + ", " + skval + " for " + uid);
            $.ajax({type: "POST", url: "gui?action=skillcustom&uid=" + uid + "&skill=" + skname + "&base=" + skval});
        });
    } else if (skillname == "-") {
        // quick roll, ask for the value only
        prompt_quickroll( function (skval) {
            console.log("Quick Rolling: " + skval + " for " + uid);
            $.ajax({type: "POST", url: "gui?action=skillcustom&uid=" + uid + "&base=" + skval});
        });
    } else {
        // canned skill
        $.ajax({type: "POST", url: "gui?action=skillsingle&uid=" + uid + "&skill=" + skillname});
    }
}

function toggleEntityVisibility(element)
{
    var uid = element.getAttribute('entity');
    $.ajax({type: "POST", url: "gui?action=toggleVisible&uid=" + uid});
}

function deleteEntity(element)
{
    var uid = element.getAttribute('entity');
    var name = element.getAttribute('entname');
    rm_confirm_dialog("Confirm Delete", "Delete " + name + "?", function() {
        $.ajax({type: "POST", url: "gui?action=delete&uid=" + uid, success: function(data) { $('#sigEntities').change(); } });
    });
}

function deleteEntityGroup(element)
{
    var uid = element.getAttribute('entity');
    var tag = element.getAttribute('enttag');
    rm_confirm_dialog("Confirm Delete", "Delete ALL entities in group " + tag + "?", function() {
        $.ajax({type: "POST", url: "gui?action=deletegroup&uid=" + uid, success: function(data) { $('#sigEntities').change(); } });
    });
}

function changeActivation(element, toLoad, loadHidden)
{
    var uid = element.getAttribute('entity');
    if (toLoad)
        $.ajax({type: "POST", url: "gui?action=activate&uid=" + uid + "&hidden=" + loadHidden});
    else
        $.ajax({type: "POST", url: "gui?action=deactivate&uid=" + uid});
}

function changeGroupActivation(element, toLoad, loadHidden)
{
    var uid = element.getAttribute('entity');
    if (toLoad)
        $.ajax({type: "POST", url: "gui?action=activatepeer&uid=" + uid + "&hidden=" + loadHidden});
    else
        $.ajax({type: "POST", url: "gui?action=deactivatepeer&uid=" + uid});
}

function playerLoadAttack(element, weaponindex)
{
    var uid = element.getAttribute('entity');
    var ent = findActiveEntity(uid);
    if (ent) {
        setInputValueById("attacker", ent.name);
        setInputValueById("sp_attacker", ent.name);
        setInputValueById("ob", ent.weapons[weaponindex].ob);
        setSelectOptionById("weaponselect", ent.weapons[weaponindex].uid);
        // NOTE: This will be an issue for the old combat view
        setSelectOptionById("rankselect", ent.weapons[weaponindex].rank);
        console.log("Loading attack for: " + ent.name + ", weapon: " + ent.weapons[weaponindex].name);
    }
}

function playerLoadDefense(element)
{
    var uid = element.getAttribute('entity');
    var ent = findActiveEntity(uid);
    if (ent) {
        setInputValueById("defender", ent.name);
        setInputValueById("sp_defender", ent.name);
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
        },
        VisibilityFormatter: function (value, template) {
            if (value)
                return 'glyphicon glyphicon-eye-open';
            else
                return 'glyphicon glyphicon-eye-close';
        }
    });
    doRMInit();
    updateEntities();
    refreshView();
    $('#sigEntities').change(function() {
        updateEntities();
    });
}

$(document).ready(callbackInit);
</SCRIPT>

<body class="rmmain">
<div id="signals" style="display: none;"><span id="sigEntities"></span></div>
<div class="rmtop bgwhite">
<div class="rmtabs">
<!-- user / logout -->
   <div class="btn-group pull-right">
    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
      <i class="glyphicon glyphicon-user"></i>&nbsp;${rm.user}
      <span class="caret"></span>
    </a>
    <ul class="dropdown-menu">
      <#if rm.permit gte 3>
      <li><a onclick="request_archive()" href="#">Clean Slate</a></li>
      </#if>
      <li><a href="/gui?action=logout">Sign Out</a></li>
    </ul>
  </div>
          
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#rmactive">Active</a></li>
<#if rm.permit gte 3>
  <li><a data-toggle="tab" href="#rmall">All</a></li>
</#if>
  <li><a data-toggle="tab" href="#rmonline">Online</a></li>
  <li><a data-toggle="tab" href="#rmcombat" id="combatview">Combat</a></li>
  <li><a data-toggle="tab" href="#rmspell" id="spellview">Spell</a></li>
</ul>
</div>
<div class="tab-content bgwhite">
  <div id="rmactive" class="tab-pane in active">
    <#include "ActiveView.ftl">
  </div>
<#if rm.permit gte 3>
  <div id="rmall" class="tab-pane">
    <div id="rmallcontents">
    </div>
  </div>
</#if>
  <div id="rmonline" class="tab-pane">
    <ul id="ulonline" />
  </div>
  <div id="rmcombat" class="tab-pane">
    <#include "CombatDialogNew.ftl">
  </div>
  <div id="rmspell" class="tab-pane">
    <#include "SpellView.ftl">
  </div>
</div>
</div>

<div class="rmmiddle">
Log
</div>

<div class="rmbottom" id="rmlog">
</div>
</div>

</body>
</html>