<#include "Macros.ftl">
<@pageinit subtitle="Login" />
<#include "PageBegin.ftl">
<body>
<#include "NavBasic.ftl">
<br />
<br />
<#if user??> <#-- Handle user already logged in -->
<div class="alert alert-info">
<strong>Already Logged In</strong>
<p>You are already logged in as <span class="badge label-important"><i class="glyphicon glyphicon-user"></i>&nbsp;${rm.user}</span>.</p>
<p><a class="btn btn-primary" href="/gui?action=logout">Logout</a></p>
</div>
<#else>
<#if urlparams.fail??>
<div class="alert alert-danger">
<strong>Login failed!<strong> Password verification failed.  Please try again.
<#else>
<div class="alert alert-info">
<strong>Login Required!</strong> Please login to access Rolemaster Web.
</#if>
</div>

<div class="row-fluid jade-psec" style="background-color:#FFFFFF" >
<form class="form-horizontal" id="login" 
 action="/gui?ftl=LoginPage.ftl" method="POST">
   <fieldset>
		 <div class="form-group">
		 	<label for="username" class="col-sm-2 control-label">Username</label>
		 	<div class="col-sm-2">
              <input id="username" type="text" class="span3" placeholder="Username" name="username" autocomplete="username" >
            </div>
          </div>
		 <div class="form-group">
		 	<label for="passwd" class="col-sm-2 control-label">Password</label>
            <div class="col-sm-2">
              <input id="passwd" type="password" class="span3" placeholder="Password" name="passwd" autocomplete="current-password">
            </div>
          </div>
        <div class="form-group">
    		<div class="col-sm-offset-2 col-sm-10">
            <button type="submit" class="btn btn-primary">Login</button>
          </div>
        </div>
	</fieldset>
</form>
</div>


<script type='text/javascript'>
	<#-- user name not known from session, try cookie -->
	var userByCookie = getCookie('rmweb.login');
	if (userByCookie)
	{
		<#-- user name already known, update it, put cursor on PW -->
		$('#username').val(userByCookie);
		document.forms.login.passwd.focus();
		document.forms.login.passwd.select();
	}
	else
	{
		<#-- user name unknown, put cursor on username -->
		document.forms.login.username.focus();
		document.forms.login.username.select();
	}
</script>
</#if> <#-- end if already logged in -->
</body>
</html>