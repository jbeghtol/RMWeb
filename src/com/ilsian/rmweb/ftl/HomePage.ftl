<#include "Macros.ftl">
<@pageinit subtitle="Home" context="home" />
<#include "PageBegin.ftl">

<#include "EditWounds.ftl">
<#include "PlayerPopup.ftl">
<#include "LogEntry.ftl">

<script id="tmplActive" type="text/html">
<table id="activetable" class="table table-dense table-striped nomargin">
<colgroup>
    <col width="13%">
    <col width="40%">
    <col width="22%">
    <col width="25%">
</colgroup>
<thead><tr><th><span class="rmexecute">Init</span><span class="rmdeclare">Phase</span></th><th>Name</th><th class="rmresult" id="activeskillresult">Result</th><th></th></tr></thead>
<tbody id="activerows"></tbody></table>
</script>

<script id="tmplActiveRow" type="text/html">
<tr data-template-bind='[{"attribute": "class", "value": "tag", "formatter": "GroupRowFormatter"}]' >
<td><span onclick="changePhase(this)" class="rmexecute" data-template-bind='[{"attribute": "title", "value": "initexplain"},{"attribute": "entity", "value": "uid"}]' data-content="phaseicon" />
<span class="rmdeclare" data-content="stageselect" title="Snap, Normal, Deliberate" /></td>
<td onclick="editWounds(this)" data-content-prepend="name" data-template-bind='[{"attribute": "title", "value": "effects.detail"},{"attribute": "entity", "value": "uid"}]' ><span data-content="effects.brief" /></td> 
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
  <a data-template-bind='[{"attribute": "entity", "value": "uid"}]' href="#" class="btn btn-xs" onclick="playerPopup(this)"><i class="glyphicon glyphicon-tasks"></i></a>
<#if false>
  <!-- This was the old dropdown code, now removed by if -->
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
</#if>
</td>
</tr>
</script>

<script id="tmplAllEntities" type="text/html">
<table id="alltable" class="table table-dense table-striped nomargin">
<thead><tr><th>Auto</th><th>Name</th><th>Load</th><th>Remove&nbsp;
<button class="btn btn-primary btn-xs" title="Upload updates to entities" onclick="rm_file_upload_dialog('sigEntities')">Import <i class="glyphicon glyphicon-floppy-open"></i></button>
<button class="btn btn-primary btn-xs" title="Sync Entity Links" onclick="rm_sync_entities('sigEntities')">Sync <i class="glyphicon glyphicon-floppy-open"></i></button>
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
    if (clist == '') {
        return true;
    }
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
                       if (data.log.events[0].event == "System reset" || data.log.events[0].event == "System restored") {
                           setElementHtml("rmlog", "");
                       }
                   }

                   // use a template, much nicer
                   $('#rmlog').loadTemplate( $('#tmplLogEvent'), data.log.events, { append: true } );
                   
                   // but, special handler to clear db links that have been used
                   for (var i=0;i<data.log.events.length; i++) {
                        if (data.log.events[i].dbclear) {
                            // this invalidates some DB link event, find it and remove its links
                            $('#rmlog').find('span[db_uid=' + data.log.events[i].dbclear + ']').hide();
                        }
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
                        
                        // finally, if this entity is currently loaded in our combat window, update those to match new info
                        updateCombatMods(data.active.records[i]);
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

function findActiveEntityByName(name) {
    for (var i=0; i<allActives.length; i++) {
        if (allActives[i].name == name) {
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
    $.confirm({ 
        escapeKey: 'cancel',
        title: "Round Advance",
        content: "Apply time effects for wounds?",
        columnClass: 'medium',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 100,
        scrollToPreviousElement: false,
        buttons: {
            conf: {
                text: 'UPDATE WOUNDS',
                keys: ['enter'],
                action: function() { doAdvanceRound('false'); }
            },
            cancel: {
                text: 'INITIATIVE ONLY',
                action: function() { doAdvanceRound('true'); }
            }
        }
    });
}

function doAdvanceRound(noef) 
{
    $.ajax({type: "POST", url: "gui?action=nextround&noeffects=" + noef});
}

function request_archive()
{
    rm_confirm_dialog("Confirm Clean", "Reset to round 1 and clear log?", function() {
       $.ajax({type: "POST", url: "gui?action=cleanslate"})
    });
}

function terminate_rmweb()
{
    rm_confirm_dialog("Confirm Shutdown", "Exit RMWeb process?", function() {
       $.ajax({type: "POST", url: "gui?action=terminate"})
        .done(function(data, textStatus, jqXHR) {
            popup_note('Stopped', 'RMWeb has been stopped.');
        });
    });
}

function create_checkpoint()
{
    prompt_string('Note', function(note) {
       $.ajax({type: "POST", url: "gui?action=checkpoint&note=" + encodeURIComponent(note)})
    });
}


function restore_archive()
{
    $.ajax({type: "POST", url: "gui?action=checkpointQuery"})
        .done(function(data, textStatus, jqXHR) {
            prompt_checkpoint(data, function(time) {
                $.ajax({type: "POST", url: "gui?action=loadslate&time=" + time})
            });
        })
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

function expandTemplateWithList(idsel, data, idlist, listtmpl, list)
{
    var div = $('<div/>').loadTemplate($(idsel), data);
    div.find(idlist).loadTemplate(listtmpl, list);
    return div.html();
}

function playerPopup(element) 
{
    var uid = element.getAttribute('entity');
    var ent = findActiveEntity(uid);
    var tempweapons = JSON.parse(JSON.stringify(ent.weapons));
    for (var i=0;i<tempweapons.length; i++) {
        tempweapons[i].index = i;
    }
    tempweapons.push( {index: -1, name: 'Spell/BAR'});
    
    var contents = expandTemplateWithList('#tmplPlayerPopup', ent, '#popupWeapons', '#tmplPlayerWeapons', tempweapons);
    //console.log("POPUP:" + uid + "," + ent.name + "," + contents);
    $.confirm({ 
        escapeKey: 'cancel',
        title: ent.name,
        content: contents,
        columnClass: 'medium',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 0,
        scrollToPreviousElement: false,
        onOpen: function () {
            var cb = this.$$cancel;
            // set each anchor's entity, easier in jquery
            this.$content.find('a').attr("entity",uid);
            // and add the close action to each anchor
            this.$content.find('a').click(function() {
                cb.trigger('click');
            });
        },
        buttons: {
            cancel: {
                text: 'CLOSE'
            }
        }
        });
}

function toggleEntityVisibility(element)
{
    var uid = element.getAttribute('entity');
    $.ajax({type: "POST", url: "gui?action=toggleVisible&uid=" + uid});
}

function alterInitiativePhase(uid, phase)
{
    $.ajax({type: "POST", url: "gui?action=alterphase&uid=" + uid + "&phase=" + phase });
}

function changePhase(element)
{
// right now, everyone can do this
<#if rm.permit gte 0>
    var ent = element.getAttribute('entity');
    $.confirm({ 
        escapeKey: 'cancel',
        title: "Change Phase",
        content: "Change phase but keep initiative?",
        columnClass: 'medium',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 100,
        scrollToPreviousElement: false,
        buttons: {
            conf: {
                text: 'NORMAL',
                keys: ['enter'],
                action: function() { alterInitiativePhase(ent,0); }
            },
            alt: {
                text: 'SNAP',
                action: function() { alterInitiativePhase(ent, -1); }
            },
            alt2: {
                text: 'DELIBERATE',
                action: function() { alterInitiativePhase(ent,1); }
            },
            cancel: {
                text: 'CANCEL'
            }
        }
    });
</#if>
}

function editWounds(element)
{
    var ent = findActiveEntity(element.getAttribute('entity'));
    var contents = expandTemplate('#tmplEditWounds', ent );
    rm_edit_wounds_dialog(contents, ent.name);
}

function applyPendingWounds(element, action)
{
    var uid = element.getAttribute('db_uid');
    $.ajax({type: "POST", url: "gui?action=pendingWound&uid=" + uid + "&dispatch=" + action});
    
    if (action == "edit") {
        var target = element.getAttribute('defender');
        if (target) {
            var ent = findActiveEntityByName(target);
            var contents = expandTemplate('#tmplEditWounds', ent );
            rm_edit_wounds_dialog(contents, ent.name);
        }      
    }
}

function rm_sync_entities(element)
{
    $.ajax({type: "POST", url: "gui?action=syncentities", success: function(data) { $('#sigEntities').change(); } });
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

function playerLoadAttack2(element)
{
    var weaponIndex = element.getAttribute('weapon');
    playerLoadAttack(element, weaponIndex);
}

function playerLoadAttack(element, weaponindex)
{
    var uid = element.getAttribute('entity');
    var ent = findActiveEntity(uid);
    if (ent) {
        setInputValueById("attacker", ent.name);
        setInputValueById("sp_attacker", ent.name);
        setInputValueById("sp_level_att", ent.level);
        if (weaponindex >= 0) {
            setInputValueById("ob", ent.weapons[weaponindex].ob);
            setSelectOptionById("weaponselect", ent.weapons[weaponindex].uid);
            setSelectOptionById("rankselect", ent.weapons[weaponindex].rank);
            console.log("Loading attack for: " + ent.name + ", weapon: " + ent.weapons[weaponindex].name);
        }
        updateCombatMods(ent);
    }
}

function updateCombatMods(ent)
{
<#if rm.cond_mods>
    if (ent.name == $('#attacker').val()) {
        // attacker has been updated
        //setInputValueById("att_cond", ent.effects.w.bonus + ent.effects.w.penalty);
        var c = document.getElementById('att_cond');
        c.value = ent.effects.w.bonus + ent.effects.w.penalty;
        var expl = ent.effects.w.bonus + " + " + ent.effects.w.penalty;
        c.setAttribute('title', expl);
    } 
    if (ent.name == $('#defender').val()) {
        // defender has been updated
        var c = document.getElementById('def_cond');
        var expl = "";
        if (ent.effects.w.stun > 0 || ent.effects.w.noparry > 0) {
            c.value = 20;
            expl = "Defender stunned, attacker at +20";
        } else {
            c.value = 0;
            expl = "Defender normal, attacker +0";
        }
        c.setAttribute('title', expl);
    } 
</#if>
}

function playerLoadDefense2(element)
{
    var weaponIndex = element.getAttribute('weapon');
    var uid = element.getAttribute('entity');
    var ent = findActiveEntity(uid);
    if (ent) {
        setInputValueById("defender", ent.name);
        setInputValueById("sp_defender", ent.name);
        setInputValueById("db", ent.db);
        setInputValueById("sp_level_def", ent.level);
        setSelectOptionById("sizeselect", ent.size);
        setSelectOptionById("specialselect", ent.special);
        
        setSelectOptionById("armorselect", ent.at);
        updateCombatMods(ent);
        console.log("Loading defense for uid: " + uid);
        if (weaponIndex < 0)
            $('#spellview').click();
        else
            $('#combatview').click();
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
        },
        DBOptionFormatter: function (value, template) {
            if (value)
                return 'rmdbopt';
            return 'rmhidden';
        },
        GroupRowFormatter: function (value, template) {
            if (value.toLowerCase() == 'players') {
                return 'rmrowpc';
            } else {
                return 'rmrownpc';
            }
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
      <li><a onclick="create_checkpoint()" href="#">Save Checkpoint</a></li>
      <li><a onclick="restore_archive()" href="#">Restore Checkpoint</a></li>
      <li><a href="gui?ftl=EditGlobals" href="#">Settings</a></li>
      <li><a onclick="terminate_rmweb()" href="#">Shutdown RMWeb</a></li>
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