
/* Methods for doing lookups */

function doLookupCritical(inRoll, inCrits, attacker, defender, validity)
{
	$.post(	"gui?action=lookupCritical", { roll: inRoll, crits: inCrits, attacker: attacker, defender: defender, validity: validity }, onCritResults );
}

function doLookupAttack(inRawRoll, inRoll, inWeapon, inArmor, rankLimit, attacker, defender, validity, explain)
{
	$.post(	"gui?action=lookupAttack", { rawRoll: inRawRoll, roll: inRoll, weap: inWeapon, at: inArmor, attacker: attacker, defender: defender, validity: validity, explain: explain, ranklimit: rankLimit }, onAttackResults );
}

function doAttack(reqdata)
{
    $.post( "gui?action=lookupAttack", reqdata, onAttackResults );
}

function doUpdateTables()
{
	$.post(	"gui?action=lookupTables", { }, onTableResults );
}

/* Callback methods after lookups */

function onCritResults(results)
{
	var e = document.getElementById('criticalresults');
	e.innerHTML = results.effects;
	$('#criticalroll').val(results.roll);
}

function onAttackResults(results)
{
    var a = document.getElementById('attackroll');
    a.value = results.roll;
    
    var b = document.getElementById('attacktotal');
    b.value = results.summation;
    b.setAttribute('title', results.explain);
     
	var e = document.getElementById('attackresult');
	e.value = results.hits + results.criticals;
	
	var f = document.getElementById('attackcritical');
	f.value = results.criticals;
}

function weaponCompare(a,b)
{
    return a.name.localeCompare(b.name);
}

function onTableResults(results)
{
	var acell = document.getElementById('armorselect');
	var wcell = document.getElementById('weaponselect');
	if (acell != null)
	{
		var data = '';
		for (var i=0;i<results.armors.length; i++)
		{
			data += '<OPTION value="' + results.armors[i] + '">AT' + results.armors[i] + '</OPTION>';
		}
		acell.innerHTML = data;
	}
	if (wcell != null)
	{
        // sort weapons, pretty like but we have to keep track of the indexes which are essentially the database ID
        var wlist = new Array();
		for (var i=0;i<results.weapons.length; i++)
		{
            var rec = {name:results.weapons[i], id: i };
            wlist[i] = rec;
		}
		wlist.sort(weaponCompare);
		
		var data = '';
		for (var i=0;i<wlist.length; i++)
		{
            data += '<OPTION value="' + wlist[i].id + '">' + wlist[i].name + '</OPTION>';
        }
            		
		wcell.innerHTML = data;
	}
}

function rollClosed()
{
	return Math.floor((Math.random()*100)+1);	
}

function rollOpen()
{
	var baseRoll = rollClosed();
	if (baseRoll > 95)
	{
		/* open end up! */
		var nextRoll = rollClosed();
		while (nextRoll > 95)
		{
			baseRoll += nextRoll;
			nextRoll = rollClosed();
		}
		baseRoll += nextRoll;
	}
	else if (baseRoll < 6)
	{
		/* open end down! */
		var nextRoll = rollClosed();
		while (nextRoll < 6)
		{
			baseRoll -= nextRoll;
			nextRoll = rollClosed();
		}
		baseRoll -= nextRoll;
	}
	return baseRoll;
}


function doFormAttackRoll()
{
	$('#attackroll').val('');
	doFormAttack(1);
}

function formAttackKey()
{
    if(event.key === 'Enter') {
        //updateAttackSum();
        doFormAttack(0);
    }
}

function getNumeric(id)
{
	var e = document.getElementById(id);
	if (e != null && e.value != '')
		return parseInt(e.value);
	return 0;
}

function setNumeric(id, val)
{
	var e = document.getElementById(id);
	if (e)
		e.value = val;
}

function updateAttackSum()
{
	var total = getNumeric('attackroll') + getNumeric('ob') + getNumeric('attackmods') - getNumeric('db');
	var explain = getNumeric('ob') + ' + ' + getNumeric('attackmods') + ' - ' + getNumeric('db') + ' + (' +  getNumeric('attackroll') + ')';
	setNumeric('attacktotal', total);
	document.getElementById('attacktotal').setAttribute('title', explain);
}

function doFormAttackOld(validity)
{
    var roll = document.getElementById('attackroll');
    var explain = document.getElementById('attackexplain');
	var r = document.getElementById('attacktotal');
	var w = document.getElementById('weaponselect');
	var a = document.getElementById('armorselect');
	var att = document.getElementById('attacker');
    var def = document.getElementById('defender');
    // extract rank limit, if it exists - default to max (3) otherwise
    var rlimitelem = document.getElementById('rankselect');
    var rlimit = 3;
    if (rlimitelem)
        rlimit = rlimitelem.options[rlimitelem.selectedIndex].value;
        
    var practice = document.getElementById('practice');
    if (practice.checked)
        validity = 2;
	doLookupAttack(roll.value, r.value, w.options[w.selectedIndex].value, a.options[a.selectedIndex].value, rlimit, att.value, def.value, validity, document.getElementById('attacktotal').getAttribute('title'));
}

function doFormAttack(validity)
{
    // extract rank limit, if it exists - default to max (3) otherwise
    var rlimitelem = document.getElementById('rankselect');
    var rlimit = 3;
    if (rlimitelem)
        rlimit = rlimitelem.options[rlimitelem.selectedIndex].value;
        
    var practice = document.getElementById('practice');
    if (practice.checked)
        validity = 2;
    
    // build the request from the form
    var req = new Object();
    req.roll = $('#attackroll').val();
    req.weap = $('#weaponselect').val();
    req.largecrit = $('#weapontype').val();
    req.at = $('#armorselect').val();
    req.attacker = $('#attacker').val();
    req.defender = $('#defender').val();
    req.ob = getNumeric('ob');
    req.mods = getNumeric('attackmods');
    req.db = getNumeric('db');
    req.parry = getNumeric('parry');
    req.validity = validity;
    req.ranklimit = rlimit;
    req.size = $('#sizeselect').val();
    req.reducecrit = $('#specialselect').val();
    doAttack(req);
}

function doFormCriticalRoll()
{
	$('#criticalroll').val('');
	doFormCritical(1);
}

function formCriticalKey()
{
    if(event.key === 'Enter') {
        doFormCritical(0);
    }
}

function doFormCritical(validity)
{
	var cr = document.getElementById('attackcritical');
	var rl = document.getElementById('criticalroll');
	var att = document.getElementById('attacker');
    var def = document.getElementById('defender');
    var practice = document.getElementById('practice');
    if (practice.checked)
        validity = 2;
	doLookupCritical(rl.value, cr.value, att.value, def.value, validity);
}

function doRMTest()
{
	doLookupCritical(95, 'ES');
}

function doRMTest2()
{
	doLookupAttack(150, 1, 1, 3);
}

function doRMInit()
{
	doUpdateTables();
//	setupPresets();
}

/*
var presets = null;
$.cookie.json = true;

function setupPresets()
{
	if (!presets)
	{
		console.log("Fetching presets from cookie...");
		presets = $.cookie('rmlookup_presets');
		if (!presets)
		{
			console.log("No presets found.");
			presets = new Array();
		}
	}
	
	var menu = document.getElementById('menupresets');
	if (presets.length == 0)
	{
		menu.innerHTML = '<li>-None-</li>';
	}
	else
	{
		var menudata = '';
		for (var i=0;i<presets.length;i++)
		{
			menudata = menudata + '<li><a onclick="loadPreset(this)" href="#">' + presets[i].name + '</a></li>'
		}
		menu.innerHTML = menudata;
	}
}

function findPreset(name)
{
	var index = 0;
	while (index < presets.length)
	{
		if (presets[index].name == name)
		{
			return index;
		}
		index++;
	}
	return -1;
}

function putPresetToForm(p)
{
	console.log('Restoring ' + p.name);
	// selects 
	document.getElementById('weaponselect').selectedIndex = p.ws;
	document.getElementById('armorselect').selectedIndex = p.as;
	
	// number values
	setNumeric('ob', p.ob);
	setNumeric('db', p.db);
	setNumeric('attackmods', p.mods);
	
	// name values
	document.getElementById('name').value = p.name;
	
}

function getPresetFromForm()
{
	// selects 
	var ws = document.getElementById('weaponselect').selectedIndex;
	var as = document.getElementById('armorselect').selectedIndex;
	
	// number values
	var ob = getNumeric('ob');
	var db = getNumeric('db');
	var mods = getNumeric('attackmods');
	
	// name values
	var name = document.getElementById('name').value;
	if (name != '')
	{
		var preset = new Object();
		preset.name = name;
		preset.ob = ob;
		preset.db = db;
		preset.mods = mods;
		preset.ws = ws;
		preset.as = as;
		return preset;
	}
	return null;
}


function loadPreset(anchor)
{
	console.log('Looking for preset: ' + anchor.innerHTML);
	var index = findPreset(anchor.innerHTML);
	if (index >= 0)
		putPresetToForm(presets[index]);
}

function savePreset()
{
	console.log('Saving preset');
	var p = getPresetFromForm();
	if (p)
	{
		var index = findPreset(p.name);
		if (index < 0)
		{
			index = presets.length;
		}
		presets[index] = p;
	}
	setupPresets();
	$.cookie('rmlookup_presets', presets);
}
*/
