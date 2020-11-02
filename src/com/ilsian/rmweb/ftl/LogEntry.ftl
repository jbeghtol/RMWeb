<script id="tmplLogEvent" type="text/html">
<div class="rmevent">
<div data-class="type" data-content-prepend="header"> 
<span style="float:right;margin-right:2px;" data-content-append="user"><i class="glyphicon glyphicon-user"></i>&nbsp;</span>
<#if rm.permit gte 2>
<span style="float:right;" data-template-bind='[{"attribute": "class", "value": "dbopt", "formatter": "DBOptionFormatter"}, {"attribute": "db_uid", "value": "dbopt"}]'>
  <a href="#" data-template-bind='[{"attribute": "db_uid", "value": "dbopt"}]' title="Apply starting this round" onclick="applyPendingWounds(this, 'now')"><span class="glyphicon glyphicon-ok"></span>&nbsp;</a>
  <a href="#" data-template-bind='[{"attribute": "db_uid", "value": "dbopt"}]' title="Apply starting next round" onclick="applyPendingWounds(this, 'next')"><span class="glyphicon glyphicon-share-alt"></span>&nbsp;</a>
  <a href="#" data-template-bind='[{"attribute": "db_uid", "value": "dbopt"}, {"attribute": "defender", "value": "target"}]' title="Edit Defender wounds directly" onclick="applyPendingWounds(this, 'edit')"><span class="glyphicon glyphicon-wrench"></span>&nbsp;</a>
  <a href="#" data-template-bind='[{"attribute": "db_uid", "value": "dbopt"}]' title="Dismiss this damage" onclick="applyPendingWounds(this, 'cancel')"><span class="glyphicon glyphicon-remove"></span>&nbsp;&nbsp;</a>
</span>
</#if>
</div>
<div class="eventresultbox" data-content="event"></div>
</div>
</script>