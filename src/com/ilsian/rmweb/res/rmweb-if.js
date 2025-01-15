
function setElementVisibility(name, value)
{
	var e = document.getElementById(name);
	if (e)
	{
		e.style.display = value;
	}
}

function setElementHtml(name, value)
{
	var e = document.getElementById(name);
	if (e)
	{
		e.innerHTML = value;
	}
}

function setElementScrollEnd(name, value)
{
    var e = document.getElementById(name);
    if (e)
    {
        e.scrollTop = e.scrollHeight;        
    }
}

function getElementHtml(name)
{
	var e = document.getElementById(name);
	if (e)
	{
		return e.innerHTML;
	}
	return null;
}

function getInputValueById(name)
{
	var e = document.getElementById(name);
	if (e)
	{
		return e.value;
	}
	return null;
}

function setInputValueById(name, value)
{
	var e = document.getElementById(name);
	if (e)
	{
		e.value = value;
	}
}

function setSelectOptionById(selectId, value)
{
	var selectElement = document.getElementById(selectId);
	if (selectElement)
	{
		var options = selectElement.options;
	    for (var i = 0, optionsLength = options.length; i < optionsLength; i++) {
	        if (options[i].value == value) {
	            selectElement.selectedIndex = i;
	            return true;
	        }
	    }
	}
    return false;
}

function setHtmlFromValueById(control, id)
{
	var e = document.getElementById(id);
	if (e)
	{
		e.innerHTML = control.value;
	}
}

function getCookie(name) {
    var v = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
    return v ? v[2] : null;
}

function setCookie(name, value, days) {
    var d = new Date;
    d.setTime(d.getTime() + 24*60*60*1000*days);
    document.cookie = name + "=" + value + ";path=/;expires=" + d.toGMTString();
}

function expandTemplate(idsel, data)
{
	return $('<div/>').loadTemplate($(idsel), data).html();
}

function toggle_input(element, inputid)
{
	var disableme = !element.checked;
	$('#' + inputid).prop("disabled", disableme);
}

function coming_soon()
{
    $.confirm({ 
    	escapeKey: 'conf',
    	title: 'Coming Soon!',
    	content: 'This feature is not yet available.',
    	columnClass: 'medium',
    	type: 'blue',
        animation: 'opacity',
        animationSpeed: 300,
	    buttons: {
		    conf: {
	            text: 'OK',
	            keys: ['enter'],
	            action: function(){
		            // nothing
	            }
	        }
	    }
	});	
}

function auto_reload()
{
	window.location.reload(true);
}

function on_json_response(data)
{
	show_va_note(data.message);
}

function show_va_note(msg)
{
	setElementVisibility('vabgheader', 'block');
	setElementHtml('vabgheader', '<div class="alert alert-info"><button class="close" data-dismiss="alert" href="#">x</button>' + msg + '</div>');
}

function show_lhem_note(msg, aclass)
{
	if (!aclass)
		aclass = 'alert-info';
	setElementVisibility('vabgheader', 'block');
	setElementHtml('vabgheader', '<div class="alert ' + aclass + '"><button class="close" data-dismiss="alert" href="#">x</button>' + msg + '</div>');
}

var autohide_running = false;
function restart_autohide()
{
	if (!autohide_running)
	{
		autohide_running = true;
		window.setTimeout(function() { check_autohide(); }, 1000);
	}
}

function check_autohide()
{
	var timeleft = $('#_alert_hide').attr('dismiss-sec');
	if (timeleft)
	{
		timeleft--;
		if (timeleft == 0)
		{
			$('#_alert_hide').click();
			autohide_running = false;
			return;
		}
		$('#_alert_count').html(timeleft + 's');
		$('#_alert_hide').attr('dismiss-sec', timeleft);
		window.setTimeout(function() { check_autohide(); }, 1000);
	}
	else
	{
		autohide_running = false;
	}
}

/*
 * show_note_autohide - Show a note in an alert header that automatically closes
 * 
 * Unhides and renders an alert field that automatically
 * removes itself if not dismissed after a give time.
 * 
 * @param msg - Message to show
 * @param timeout - [int - optional] seconds to remain active (default 3s)
 */
function show_note_autohide(msg, timeout)
{
	if (!timeout)
		timeout = 3;
	var divid = 'floatnote';
	setElementVisibility(divid, 'block');
	setElementHtml(divid, 
			'<div class="floatnote alert">' +
				'<div class="row">' +
					'<div class="col-xs-10 enlarge">' + msg + '</div>' +
					'<div class="col-xs-2">' + 
						'<button id="_alert_hide" dismiss-sec="' + timeout + '" class="close white" data-dismiss="alert" href="#"><i class="glyphicon glyphicon-remove-circle white"></i></button>' +
					'</div>' +
				'</div>' +
				'<p	id="_alert_count" style="font-size: 80%;position: absolute; bottom:8px; right:14px;opacity:0.5;" >3s</p>' +
			'</div>');
	restart_autohide();
}


function arrayToCSV(arr)
{
	var csv = "";
	for (var i=0; i<arr.length; i++) {
		if (csv.length>0)
			csv = csv + ',' + arr[i];
		else
			csv = arr[i];
	}
	return csv;
}


function prompt_custom_skill(callback)
{

    $.confirm({
        title: 'Skill Check',
        escapeKey: 'cancel',
        content: '<div class="form-group"><label class="control-label">Skill Name</label><input autofocus type="text" id="input-row"><br><label class="control-label">Skill</label><input type="number" id="input-val" ></div>',
        columnClass: 'small',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 100,
        buttons: {
                ok: {
                   text: 'Roll',
                   keys: ['enter'],
                   action: function () {
                       var skName = this.$content.find('input#input-row').val().trim();
                       var skVal = this.$content.find('input#input-val').val().trim();
                       if (!skName) {
                            $.alert({
                                content: "Please enter a skill name.",
                                type: 'red'
                                });
                            return false;
                       } else if (skName.match(/^[0-9a-z]+$/)) {
                            $.alert({
                                content: "No weird characters in a skill name.",
                                type: 'red'
                                });
                            return false;
                       } else if (!skVal) {
                            $.alert({
                                content: "Please enter a skill value.",
                                type: 'red'
                                });
                            return false;
                       } else {
                            callback(skName, skVal);
                       }
                   }
               },
               cancel: function () {
                  // do nothing.
               }
           }
        });
 
}

var currQuickroll = null;

function screenEnter(input, event)
{
    var key = event.which;
    if (key == 13 && currQuickroll) {
        var skVal = currQuickroll.$content.find('input#input-row').val().trim();
        if (!skVal) skVal = 0;
        currQuickroll.rmcallback(skVal);
        currQuickroll.close();
    }
} 

function prompt_quickroll(callback)
{
    currQuickroll = $.confirm({
        rmcallback: callback,
        title: 'Quick Roll',
        escapeKey: 'cancel',
        content: '<div class="form-group"><label class="control-label">Base</label><input autofocus type="number" id="input-row" class="form-control" onkeydown="screenEnter(this, event)"></div>',
        columnClass: 'small',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 100,
        buttons: {
                ok: {
                   text: 'Roll',
                   keys: ['enter'],
                   action: function () {
                       var skVal = this.$content.find('input#input-row').val().trim();
                       if (!skVal) skVal = 0;
                       callback(skVal);
                   }
               },
               cancel: function () {
                  // do nothing.
               }
           }
        });
 
}

function rm_sync_entities(signalId) {
    
}

function rm_file_upload_dialog(signalId) {
    // let the dialog do its thing
    $.confirm({ 
        escapeKey: 'cancel',
        content: 'url:/gui?ftl=DialogUpload&type=csv',
        onContentReady: function(data, status, xhr) {
            onDialogLoad(this);
        },
        onDestroy: function() {
            if (signalId)
                onDialogSignal(signalId);
        },
        columnClass: 'medium',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 300,
        autoResize: false,
        buttons: {
            conf: {
                text: 'Upload',
                isDisabled: true,
                keys: ['enter'],
                action: function() {
                    return false;
                }
            },
            cancel: {
                text: 'Cancel',
                action: function(){
                    // nothing
                }
            }
        }
    });
}

function rm_edit_wounds_dialog(contents, name) {
    $.confirm({ 
        title: 'Wounds: ' + name,
        escapeKey: 'cancel',
        content: contents,
        columnClass: 'medium',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 300,
        autoResize: false,
        buttons: {
            conf: {
                text: 'OK',
                keys: ['enter'],
                action: function() {
                    console.log("ENTER");
                    var form = this.$content.find('form#form_wound');
                    var url = "gui?action=updateWounds";
                       
                    $.ajax({
                           type: "POST",
                           url: url,
                           data: form.serialize(), // serializes the form's elements.
                           success: function(data)
                           {
                              console.log("Updated wounds successful.");
                           }
                         });
                    return true;
                }
            },
            reset: {
                text: 'Clear All',
                action: function() {
                    var url = "gui?action=updateWounds";
                    var nameonly = new Object();
                    nameonly.name = this.$content.find('input#wnd_name').val();
                    $.ajax({
                           type: "POST",
                           url: url,
                           data: nameonly,
                           success: function(data)
                           {
                              console.log("Cleared wounds successful.");
                           }
                         });
                    return true;
                }
            },
            cancel: {
                text: 'Cancel',
                action: function(){
                    // nothing
                }
            }
        }
    });
}

function rm_confirm_dialog(myTitle, myMessage, mySuccessHandler)
{
    $.confirm({ 
        escapeKey: 'cancel',
        title: myTitle,
        content: myMessage,
        columnClass: 'medium',
        type: 'blue',
        animation: 'opacity',
        animationSpeed: 300,
        scrollToPreviousElement: false,
        buttons: {
            conf: {
                text: 'OK',
                keys: ['enter'],
                action: mySuccessHandler
            },
            cancel: function () {
                // no-op
            }
        }
    });
}
