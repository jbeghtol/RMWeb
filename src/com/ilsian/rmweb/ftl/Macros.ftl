<#-- Global headers we want for all -->
<#setting number_format="computer"><#-- 123456 instead of 123,456 -->

<#macro pageinit title="RM Web" subtitle="" context="">
	<#assign apptitle=title>
	<#assign appsubtitle=subtitle>
	<#assign appcontext=context>
</#macro>

<#------------------------
  FORM RM WEB HELPERS
-------------------------->
<#macro playerskill skill skillname>
    <a class="btn btn-primary" href="#" onclick="playerSkillCheck('${skill}', this)">${skillname}</a>
</#macro>

<#------------------------
  FORM LAYOUT MACROS
-------------------------->
<#macro settingblock_begin name="UNK">
<div class="row lhem-setrow">
  <div class="col-xs-1 lhem-set-category">${name}</div>
  <div class="col-xs-11">
</#macro>

<#macro settingblock2_begin name="UNK">
<div class="row-fluid">
  <div class="col-xs-3 lhem-set-category">${name}</div>
  <div class="col-xs-9">
</#macro>

<#macro settingblock_end>
  </div>
</div>
</#macro>

<#macro settingblock_commit button="Commit">
<div class="row lhem-emptyrow">
  <div class="col-xs-1">&nbsp;</div>
  <div class="col-xs-11">
	<div class="form-actions">
		<button type="submit" class="btn btn-primary">${button}</button>
	</div>
  </div>
</div>
</#macro>

<#macro settingblock_bool name="UNK" formname="" value="">
<label class="contol-label" >${name}</label>
<div class="form-group">	
	<select class="form-control" name="${formname}"><option value="0">Off</option><option value="1" <#if value>selected</#if>>On</option></select>
</div>
</#macro>

<#macro settingblock_string name="UNK" formname="" value="" type="text" lclass="" addon="" >
<label class="contol-label ${lclass}" >${name}</label>
<div class="form-group<#if addon!=""> input-group</#if>">
	<input type="${type}" class="form-control" id="${formname}__id" name="${formname}" value="${value}" >
	<#if addon!="">
	<span class="input-group-addon">${addon}</span>
	</#if>
</div>
</#macro>

<#macro settingblock_stringopt name="UNK" formname="" value="" type="text" lclass="" addon="" checked="yes">
<#if checked=="yes">
	<input type="checkbox" id="toggle_${formname}" onchange="toggle_input(this, '${formname}__id')" checked>
	<label class="contol-label ${lclass}" for="toggle_${formname}" >${name}</label>
<#else>
	<input type="checkbox" id="toggle_${formname}" onchange="toggle_input(this, '${formname}__id')">
	<label class="contol-label ${lclass}" for="toggle_${formname}" >${name}</label>
</#if>
<div class="form-group<#if addon!=""> input-group</#if>">
	<input type="${type}" class="form-control" id="${formname}__id" name="${formname}" value="${value}" <#if checked=="no">disabled</#if> >
	<#if addon!="">
	<span class="input-group-addon">${addon}</span>
	</#if>
</div>
</#macro>

<#macro settingblock_select name="UNK" formname="" lclass="" value="" values="" ro="" useindex=false>
<label class="contol-label ${lclass}" >${name}</label>
<div class="form-group">	
	<select class="form-control" id="${formname}__id" name="${formname}" ${ro}>
	<#list values?split(",") as x>
		<#if useindex>
			<option value="${x?index}" <#if x?index?c==value>selected</#if>>${x}</option>
		<#else>
			<option value="${x}" <#if x==value>selected</#if>>${x}</option>
		</#if>
	</#list>
	</select>
</div>
</#macro>

<#macro settingblock_dir name="UNK" formname="" value="" >
<label class="contol-label" >${name}</label>
<div class="input-group form-group">
	<input type="text" readonly="readonly" class="form-control" name="${formname}" id="fileapi_${formname}" value="${value}" >
	<span class="input-group-btn"><button onclick="query_remote_file('Select ${name}','fileapi_${formname}', true)" class="btn btn-default" type="button">...</button></span>
</div>
</#macro>

<#macro settingblock_slider name="UNK" formname="" value="" changemethod="setHtmlFromValueById" min=0 max=100 step=1>
<label class="contol-label" >${name}</label>
<div class="input-group form-group">
	<span class="input-group-addon" id="sldapi_${formname}">${value}</span>
	<input type="range" min="${min}" max="${max}" step="${step}" class="form-control" id="${formname}" name="${formname}" oninput="${changemethod}(this, 'sldapi_${formname}')" value="${value}" >
</div>
</#macro>

<#------------------------
 PAGINATION HELPERS
-------------------------->
<#macro pagecontrols id max onchange>
<span id="${id}" class="btn-sm" style="display:none;" >
  <button id="${id}_prev" class="btn btn-primary btn-sm" title="Previous" onclick="${id}_prevPage()"><i class="glyphicon glyphicon-triangle-left"></i></button>
  <button id="${id}_next" class="btn btn-primary btn-sm" title="Next" onclick="${id}_nextPage()"><i class="glyphicon glyphicon-triangle-right"></i></button>
  <span id="${id}_counts" class="dl-fg bold"></span>
</span>
<span id="${id}_wait" class="btn-sm lhloader" style="display:none;" >
</span>
<script language="JavaScript">
var ${id}_index = 1;
var ${id}_lastcount = 1;
function ${id}_updatePagination(count) {
	console.log("${id}_updatePagination(" + count + ")");
	$('#${id}_wait').hide();
	${id}_lastcount = count;
	if (count > ${max})
	{
		var last_page = 1 + Math.floor(count / ${max});
		if (${id}_index > last_page)
			${id}_index = last_page;
			
		var start_rec = (${id}_index - 1) * ${max} + 1;
		var last_rec = start_rec + ${max} - 1;
		if (last_rec > count)
			last_rec = count;

		console.log("SHOWING PAGINATION: " + ${id}_index + '/' + last_page);
			
		$('#${id}').show();
		$('#${id}_prev').prop("disabled", ${id}_index == 1);
		$('#${id}_next').prop("disabled", ${id}_index == last_page);
		$('#${id}_counts').html(start_rec + '-' + last_rec + ' of ' + count);
	}
	else
	{
		console.log("HIDING PAGINATION");
		${id}_index = 1
		$('#${id}').hide();
	}
	${onchange}();
}
function ${id}_waitPagination() {
	$('#${id}').hide();
	$('#${id}_wait').show();
}
function ${id}_prevPage() {
	if (${id}_index > 1)
	{
		${id}_index--;
		${id}_updatePagination(${id}_lastcount);
	}
}
function ${id}_nextPage() {
	${id}_index++;
	${id}_updatePagination(${id}_lastcount);
}
</script>
<#assign pagecontrols_update="$id_updatePagination">
<#assign pagecontrols_update="$id_waitPagination">
</#macro>

<#macro pagecontrols_tmpl_opt id max>
{ paged: true, pageNo:${id}_index, elemPerPage: ${max} }
</#macro>

<#------------------------
  SIMPLE QUERY FTL MACRO
  - Renders a Table from an SQL query
-------------------------->
<#macro query_table actor sql>
<table class="table table-condensed table-striped"><thead><tr>
<#list actor.executeStatement(sql) as trd>
<th>${trd}</th>
</#list>
</tr>
<#list actor.rows as tr>
<tr>
  <#list tr as td>
	<td>${td}</td>
  </#list>
</tr>
</#list>
</table>
${actor.release}
</#macro>

<#------------------------
  SIMPLE QUERY FTL MACRO
  - Renders Table ROWS from an SQL query
-------------------------->
<#macro query_trows actor sql>
<table class="table table-condensed table-striped"><thead><tr>
<#list actor.executeStatement(sql) as trd>
</#list>
</tr>
<#list actor.rows as tr>
<tr>
  <#list tr as td>
	<td>${td}</td>
  </#list>
</tr>
</#list>
${actor.release}
</#macro>

<#------------------------
  SIMPLE QUERY TO MAP MACRO
  - Renders a javascript map for all values from the first column of an SQL query
  - Name is forced to lower case for case-insensitive lookups
-------------------------->
<#macro query_js_map jsname actor sql>
var ${jsname} = new Map();
<#list actor.executeStatement(sql) as trd>
</#list>
<#list actor.rows as tr>
	${jsname}.set('${tr[0]}'.toLowerCase(), true);
</#list>
${actor.release}
</#macro>