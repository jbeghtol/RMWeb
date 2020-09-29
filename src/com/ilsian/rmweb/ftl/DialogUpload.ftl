<#include "Macros.ftl">
<#-- This is an HTML fragment designed to live inside a dialog -->
<html>
<body>
<div id="uploadhint">
</div>
<div id="progressdiv" class="progress" style="display:none;">
  <div id="progress" class="progress-bar progress-bar-striped enlarged" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">
  </div>
</div>
<div id="resultinfo" class="alert" role="alert" style="display:none;" >
</div>

<#assign dialogtitle='Upload New Entities' />
<#assign dialoghint='Upload new players, NPCS, and monsters defined in CSV files.' />
<input id="fileupload" class="hiddenfile" type="file" name="files[]" accept=".csv" data-url="/gui?action=upload&type=csv" multiple>
<label id="uploadbutton" for="fileupload" class="btn btn-success">Select CSV File(s)</label>

<label id="fileselectinfo">No files selected.</label>

<script language="JavaScript">

	// holds the outer confirm dialog that we live inside
	var mydialog;
	// holds the upload async actor during upload
	var myupload;
	// track if we need to signal - eg, did anything happen
	var sigNeeded = false;
	
    function onDialogLoad(dialog) {
    	mydialog = dialog;
    	mydialog.setTitle('${dialogtitle}');
    	<#if dialoghint??>
    		$("#uploadhint").html('<p class="lhem-hint"><i class="glyphicon glyphicon-export enlarged"></i>&nbsp;${dialoghint}</p>');
    	</#if>
    }
    
    function onDialogSignal(signalId) {
    	if (sigNeeded) {
    		$('#' + signalId).change();
    	}
    }
    
    $('#fileupload').fileupload({
        dataType: 'json',
        singleFileUploads: false,
        error: function (jqXHR, textStatus, errorThrown) { 
        	var errorMessage = "Unknown error";
        	if (errorThrown)
        		errorMessage = errorThrown;
        	else if (textStatus == "abort")
        		errorMessage = "Upload canceled";
			else if (textStatus == "timeout")
        		errorMessage = "Upload timed out";
        		
        	$('#progress').removeClass("active");
        	$('#progress').addClass("progress-bar-danger");
        	$('#progress').html("Upload Error");
        	$('#resultinfo').addClass("alert-danger");
        	$('#resultinfo').html("<strong>Upload Failed</strong><ul><li>" + errorMessage + "</li></ul>");
        	$('#resultinfo').show();
            mydialog.buttons.conf.disable();
            mydialog.buttons.cancel.setText('Close');
            mydialog.buttons.cancel.action = function() { };
        },
        add: function (e,data) {
        	$('#fileselectinfo').html( data.files.length + " files to upload.");
        	mydialog.buttons.conf.enable();
        	mydialog.buttons.conf.action = function() {
        	    // get extra form data
//        	    data.formData = getExtraFormData();
//      	    if (!data.formData)
//       	    	return false;
        	    	
        		// disable this button and file selection controls once upload starts
        	    mydialog.buttons.conf.disable();
        	    $('#fileupload').attr("disabled", "true");
        	    $('#uploadbutton').attr("disabled", "true");
        	    $('#progressdiv').show();
        	    $('#progress').addClass("active");
        	    $('#fileselectinfo').html("");
        	    sigNeeded = true; // remember upload attempted
        		myupload = data.submit();
        		mydialog.buttons.cancel.action = function() {
        			myupload.abort();
        			return false;
        		};
        		return false;
        	};
        },
     	progressall: function (e, data) {
        	var progress = parseInt(data.loaded / data.total * 100, 10);
			$('#progress').css(
            	'width',
            	progress + '%'
        	);
        	if (progress == 100)
        	{
        		$('#progress').html("Upload completed.  Processing data.");
        	}
    	},        
        done: function (e, data) {
        	$('#progress').removeClass("active");
        	var msgPrefix;
        	var okCount = 0;
        	var failCount = 0;
        	var messageData = "";
        	for (var i=0; i<data.result.length; i++)
        	{
        		if (data.result[i].result) okCount++;
        		else failCount++;
        		if (data.result.length > 1)
        			messageData += "[" + i + "] " + data.result[i].message;
        		else
        			messageData += data.result[i].message;
        	}
        	
        	if (failCount == 0)
        	{
				$('#progress').addClass("progress-bar-success");
				$('#resultinfo').addClass("alert-success");
				msgPrefix = "Completed";
			}
			else if (okCount == 0)
			{
        		$('#progress').addClass("progress-bar-danger");
				$('#resultinfo').addClass("alert-danger");
        		msgPrefix = "Failed";
        	}
        	else
        	{
        		$('#progress').addClass("progress-bar-warning");
				$('#resultinfo').addClass("alert-warning");
        		msgPrefix = "Partially Completed";
        	}

        	$('#progress').html(msgPrefix);
        	$('#resultinfo').html(messageData);
        	$('#resultinfo').show();
            mydialog.buttons.conf.disable();
            mydialog.buttons.cancel.setText('Close');
            mydialog.buttons.cancel.action = function() { };
        }
    });
</script>

</body>
</html>    