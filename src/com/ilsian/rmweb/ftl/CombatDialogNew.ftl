<table id="activetable" class="table table-dense table-striped">
<tr>
<th class="att"><input type="text" class="input span1" id="attacker" placeholder="Attacker Name" /></th>
<th class="def"><input type="text" class="input span1" id="defender" placeholder="Defender Name" /></th>
</tr>

<tr>
<td class="att">
<select id="weaponselect"></select><select id="rankselect">
<option value="0">R1/S</option>
<option value="1">R2/M</option>
<option value="2">R3/L</option>
<option value="3" selected>R4/H</option>
</select>
</td>
<td class="def">
<select id="armorselect"></select><span class="add-on">DB</span><input type="number" id="db" class="shortnum" />
</td>
</tr>

<tr>
<td class="att">
Type<select id="weapontype">
<option value="A">Normal</option>
<option value="B">Magic</option>
<option value="C">Mithril</option>
<option value="D">Holy</option>
<option value="E">Slaying</option>
</select>
<span class="add-on">Mods</span><input type="number" id="attackmods" class="shortnum" />
</td>
<td class="def">
Size<select id="sizeselect">
<option value="0">Normal</option>
<option value="1">Large</option>
<option value="2">SuperLg</option>
</select>
Special<select id="specialselect">
<option value="0">None</option>
<option value="1">Crits -1</option>
<option value="2">Crits -2</option>
</select>
</td>
</tr>

<tr>
<td class="att">
<span class="add-on">OB-A</span><input type="number" id="ob" class="shortnum" />
<span class="add-on">Cond.</span><input type="number" id="att_cond" class="shortnum" disabled />
</td>
<td class="def">
<span class="add-on">OB-D</span><input type="number" id="parry" class="shortnum" />
<span class="add-on">Cond.</span><input type="number" id="def_cond" class="shortnum" disabled />
</td>
</tr>

<tr>
<td class="combat" colspan="2" style="text-align: center;" >
Fight! <input type="checkbox" id="practice" name="practice" value="practice"> practice
</td>
</tr>

<tr>
<td class="combat" colspan="2" >
<button onclick="doFormAttackRoll()" class="btn btn-primary" type="button">Roll</button>
<input type="number" class="shortnum" id="attackroll" onkeydown="formAttackKey(this)" />
Total
<input type="number" class="shortnum" id="attacktotal" disabled />
Result
<input type="text" class="input" id="attackresult" disabled />
</td>
</tr>

<tr>
<td class="combat" colspan="2" >
Critical <input type="text" class="input" id="attackcritical" />
<button onclick="doFormCriticalRoll()" class="btn btn-primary" type="button">Roll</button>
<input type="number" class="shortnum" id="criticalroll" onkeydown="formCriticalKey(this)" />
</td>
</tr>

<tr>
<td class="combat" colspan="2" >
<div class="crit" id="criticalresults">
</div>
</td>
</tr>
</table>