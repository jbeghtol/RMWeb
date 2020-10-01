
<table id="activetable" class="table table-dense table-striped">
<tr>
<th><input type="text" class="input span1" id="attacker" placeholder="Attacker Name" /></th>
<th><input type="text" class="input span1" id="defender" placeholder="Defender Name" /></th>
</tr>
<tr>
<td>
<select id="weaponselect"></select><select id="rankselect"></select>
</td>
<td>
<select class="span1" id="armorselect"></select><span class="add-on">DB</span><input type="number" class="input span1" id="db" />
</td>
</tr>
<tr>
<td>
<span class="add-on">OB-A</span><input type="number" class="input span1" id="ob" />
</td>
<td>
<span class="add-on">OB-D</span><input type="number" class="input span1" id="parry" />
</td>
</tr>
<tr>
<td>
<span class="add-on">Mods</span><input type="number" class="input span1" id="attackmods" />
</td>
<td>
SPECIAL
</td>
</tr>
</table>

<div class="control-group">
    <label class="control-label" for="opponents">Opponents</label>
    <div class="controls">
        <input type="checkbox" id="practice" name="practice" value="practice"> test)
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="ob">Offense/Defense</label>
    <div class="controls">
        <div class="input-prepend">
        </div>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="attackmods">Mods/Roll</label>
    <div class="controls">
        <div class="input-prepend">
            <button onclick="doFormAttackRoll()" class="btn btn-primary" type="button">Roll</button><input type="number" class="input span1" id="attackroll" onkeydown="formAttackKey(this)" />
        </div>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="attackmods">Results</label>
    <div class="controls">
        <input type="number" class="input span1" id="attacktotal" />
        <input type="text" class="input span1" id="attackresult" disabled />
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="attackcritical">Results</label>
    <div class="controls">
        <div class="input-prepend">
            <span class="add-on">Crit</span><input type="text" class="input span1" id="attackcritical" />
            <button onclick="doFormCriticalRoll()" class="btn btn-primary" type="button">Roll</button><input type="number" class="input span1" id="criticalroll" onkeydown="formCriticalKey(this)" />
        </div>
    </div>
</div>

<div class="control-group">
    <div class="controls span3">
        <div id="criticalresults">
        </div>
    </div>
</div>

</fieldset>
</form>
</div>

<div id="output"></div>
