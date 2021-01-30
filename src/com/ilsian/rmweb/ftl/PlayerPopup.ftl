<script id="tmplPlayerPopup" type="text/html">
<table class="table table-dense table-striped">
<#if rm.permit gte 2>
<tr><th>Level</th><td><span data-content-append="level" />&nbsp;(Hits: <span data-content-append="hits" />)</td></tr>
<#else>
<tr><th>Level</th><td data-content-append="level" /></tr>
</#if>
<tr><th>AT(DB)</th><td><span data-content-append="at" />&nbsp;(<span data-content="db"></span>)</td></tr>
<tr><th>Cond</th><td data-content-prepend="effects.detail" >&nbsp;
<#if rm.permit gte 2>
<a href="#" onclick="editWounds(this)"><i class="glyphicon glyphicon-wrench"></i></a>
</#if>
</td></tr>
<tr><th>Combat</th><td>
<table class="table table-dense table-striped" id="popupWeapons">
</table>
</td></tr>
<tr><th rowspan="4">Skills</th><td>
<@playerskill skill="alertness" skillname="Alertness" />
<@playerskill skill="combatawareness" skillname="Combat Awareness" />
</td></tr>
<tr><td>
<@playerskill skill="observation" skillname="Observation" />
<@playerskill skill="powerperception" skillname="Power Perc." />
</td></tr>
<tr><td>
<@playerskill skill="breakstun" skillname="Break Stun" />
</td></tr>
<tr><td>
<@playerskill skill="_" skillname="[Custom]" />
</td></tr>
</table>
</script>

<script id="tmplPlayerWeapons" type="text/html">
<tr>
<td class="tcenter"><a class="btn btn-danger" data-template-bind='[{"attribute": "weapon", "value": "index"}]' href="#" onclick="playerLoadAttack2(this)">Attack</a></td>
<td class="tcenter"><span class="bold" data-content="name"/>&nbsp;<span data-content="ob"/></td>
<td class="tcenter"><a class="btn btn-warning" data-template-bind='[{"attribute": "weapon", "value": "index"}]' href="#" onclick="playerLoadDefense2(this)">Defend</a></td>
</tr>
</script>

