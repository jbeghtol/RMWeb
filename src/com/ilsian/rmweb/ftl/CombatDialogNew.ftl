
<table id="activetable" class="table table-dense table-striped">
<tr>
<th><input type="text" class="input span1" id="attacker" /></th>
<th><input type="text" class="input span1" id="defender" /></th>
</tr>
<tr>
<td>
    <input type="number" class="input" id="obSkillId" oninput="obOutputId.max = obSkillId.value" />
    <input type="range" name="obInputName" id="obInputId" value="100" min="0" max="100" oninput="obOutputId.value = obInputId.value">
    <output name="obOutputName" id="obOutputId">100</output>
</td>
<td>
</td>
</tr>
</table>

<div class="control-group">
    <label class="control-label" for="opponents">Opponents</label>
    <div class="controls">
        <input type="checkbox" id="practice" name="practice" value="practice"> test)
    </div>
</div>
<div class="slidecontainer">
  <input type="range" min="1" max="100" value="50" class="slider" id="myRange">
</div>
<div class="control-group">
    <label class="control-label" for="weaponselect">Weapon/Armor</label>
    <div class="controls">
        <select class="span2" id="weaponselect"></select>
        <select class="span1" id="armorselect"></select>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="ob">Offense/Defense</label>
    <div class="controls">
        <div class="input-prepend">
            <span class="add-on">OB</span><input type="number" class="input span1" id="ob" />
            <span class="add-on">DB</span><input type="number" class="input span1" id="db" />
        </div>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="attackmods">Mods/Roll</label>
    <div class="controls">
        <div class="input-prepend">
            <span class="add-on">Mods</span><input type="number" class="input span1" id="attackmods" />
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
