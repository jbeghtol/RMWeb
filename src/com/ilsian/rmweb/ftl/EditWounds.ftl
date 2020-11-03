<script id="tmplEditWounds" type="text/html">
<div>
<form id="form_wound">
<input type="hidden" name="name" id="wnd_name" type="text" data-template-bind='[{"attribute": "value", "value": "name"}]' />
<table class="table table-dense table-striped">
<tr><th>Hits</th><th>Bleeding</th><th>Stun</th><th>No Parry</th><th>Must Parry</th></tr>
<tr>
<td><input name="hits" type="number" id="wnd_hits" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.hits"}]' /></td>
<td><input name="bleed" type="number" id="wnd_bleed" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.bleed"}]' /></td>
<td><input name="stun" type="number" id="wnd_stun" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.stun"}]' /></td>
<td><input name="noparry" type="number" id="wnd_noparry" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.noparry"}]' /></td>
<td><input name="mustparry" type="number" id="wnd_mustparry" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.mustparry"}]' /></td>
</tr>
<tr><th>Bonus</th><th>Duration</th><th></th><th>Penalty</th><th>Duration</th></tr>
<tr><td><input name="bonus" type="number" id="wnd_bonus" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.bonus"}]' /></td><td><input name="bonusdur" type="number" id="wnd_bonusdur" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.bonusdur"}]' /></td>
<td></td><td><input name="penalty" type="number" id="wnd_penalty" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.penalty"}]' /></td><td><input name="penaltydur" type="number" id="wnd_penaltydur" class="shortnum" data-template-bind='[{"attribute": "value", "value": "effects.w.penaltydur"}]' /></td></tr>
</table>
</form>
</div>
</script>