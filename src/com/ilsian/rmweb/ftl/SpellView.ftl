<table id="spelltable" class="table table-dense table-striped">
<tr>
<th class="att"><input type="text" class="input span1" id="sp_attacker" placeholder="Attacker Name" /></th>
<th class="def"><input type="text" class="input span1" id="sp_defender" placeholder="Defender Name" /></th>
</tr>

<tr>
<td class="att">
<span class="add-on">Level</span> <input type="number" id="sp_level_att" class="shortnum" />
</td>
<td class="def">
<span class="add-on">Level</span> <input type="number" id="sp_level_def" class="shortnum" />
</td>
</tr>

<tr>
<td class="att">
<span class="add-on">Ranks</span> <input type="number" id="sp_ranks" class="shortnum" />
<span class="add-on">BAR Mod</span> <input type="number" id="sp_bonus" class="shortnum" />
</td>
<td class="def">
<span class="add-on">RR Mod</span> <input type="number" id="sp_rr_bonus" class="shortnum" />
</td>
</tr>

<tr>
<td class="combat" colspan="2" style="text-align: center;" >
Base Attack Roll
</td>
</tr>

<tr>
<td class="combat" colspan="2" >
<button onclick="doFormBAR()" class="btn btn-primary" type="button">Roll</button>
<input type="number" class="shortnum" id="sp_bar_roll" onkeydown="formBARKey(this)" />
Total
<input type="number" class="shortnum" id="sp_bar_total" disabled />
Modifier
<input type="text" class="input" id="sp_bar_result" disabled />
</td>
</tr>

<tr>
<td class="combat" colspan="2" style="text-align: center;" >
Resistance Roll
</td>
</tr>

<tr>
<td class="combat" colspan="2" >
<button onclick="doFormRR()" class="btn btn-primary" type="button">Roll</button>
<input type="number" class="shortnum" id="sp_rr_roll" onkeydown="formRRKey(this)" />
Mod
<input type="text" class="shortnum" id="sp_rr_mod" />
Total
<input type="number" class="shortnum" id="sp_rr_total" disabled />
Target
<input type="text" class="shortnum" id="sp_rr_target" disabled />
</td>
</tr>

<tr>
<td class="combat" colspan="2" >
<div class="crit" id="sp_rr_results">
</div>
</td>
</tr>
</table>