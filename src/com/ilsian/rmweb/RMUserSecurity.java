package com.ilsian.rmweb;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.UserSecurity;
/**
 * RMWeb User Security - We need something to keep players from being dicks.
 * @author justin
 *
 */
public class RMUserSecurity implements UserSecurity {

	private static final String [] _ROLE_NAMES = { "Invalid", "Player", "GM", "Admin" };
	public static final int kLoginInvalid = 0;
	public static final int kLoginPlayer = 1;
	public static final int kLoginGM = 2;
	public static final int kLoginAdmin = 3;
	
	/**
	 * Get the current signed in user information from a request (session).  And
	 * saves pertinent information into the session.  When no user is logged in,
	 * and Login related resources are being requested, the security system will 
	 * provide a dummy user with kLoginInvalid permission level to allow LoginPage
	 * FTL resources to be rendered.
	 * 
	 * SESSION Effects: If user cannot be found, saves the request URL so the desired
	 * URL can be opened after a successful login.  
	 *  
	 * @return The current user info, or null if not signed in  
	 * @throws IOException 
	 */
	@Override
	public UserInfo getUserInfo(HttpServletRequest req, HttpServletResponse response) throws IOException {
		final HttpSession session = req.getSession();
		final UserInfo currUser = (UserInfo) session.getAttribute(Webspace.CURR_USER);
		
		// if we don't have a user, save the request URL to redirect back to after login
		if (currUser == null)
		{
			final String targetQuery = req.getQueryString();
			final StringBuffer targetURL = req.getRequestURL();
			if (targetQuery != null)
			{
				targetURL.append("?");
				targetURL.append(targetQuery);
			}
			
			if (req.getServletPath().equals("/gui") && 
				targetQuery != null &&
				targetQuery.startsWith("ftl=" + getLoginFTL()))
			{
				// all URLs for our servlet starting with our Login FTL should be
				// allowed by returning an invalid user instead of null
				return new UserInfo("login", UserInfo.kLoginInvalid);
			}
			else
			{
				// other URL requests are saved in our session, then user is redirected to the Login page
				session.setAttribute(Webspace.LOGIN_TARGET, targetURL.toString());
				response.sendRedirect(response.encodeRedirectURL("/gui?ftl=" + getLoginFTL()));		
			}	
		}
		return currUser;
	}

	/**
	 * Handle a security failure - if the core framework detects a resource
	 * request by an unpermitted user, this is invoked to redirect to the
	 * appropriate error page.
	 */
	@Override
	public void loginSecurityRedirect(HttpServletResponse response,
			int reqLevel, int currLevel) throws IOException {
		response.sendRedirect(response.encodeRedirectURL(
				"/gui?ftl=SecurityFail&need=" + UserInfo.getRoleName(reqLevel) + 
				"&has=" + UserInfo.getRoleName(currLevel)));
	}

	/**
	 * Return the FTL template name for the login page.
	 */
	String getLoginFTL() {
		return "LoginPage.ftl";
	}


	/**
	 * Handle the specific "login" post action to log a user into the system. If
	 * this method returns true, caller should stop any other POST handling as
	 * redirects have already been written.
	 * 
	 * SESSION Effects: On login success, user information is stored to the SESSION.  If a
	 * previous URL target was stored in the SESSION, it is used as a redirect target after
	 * the login completes.  
	 * 
	 * @return True if POST matches the login action
	 */
	@Override
	public void handleAction(String action, UserInfo user,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		final String username = request.getParameter("username");
		final String passwd = request.getParameter("passwd");
		if (setUserSession(request, response, username, passwd))
		{
			String target = (String) request.getSession().getAttribute(Webspace.LOGIN_TARGET);
			if (target != null)
			{
				// if we have a previous target that required login, go back there
	            response.sendRedirect(target);
			}
			else
			{
				response.sendRedirect("/gui");
			}
		}
		else
		{
			response.sendRedirect("/gui?ftl=" + getLoginFTL() + "&fail=password");
		}
	}


	int validatePermission(String user, String password)
	{
		if (user.equalsIgnoreCase("greg"))
			return kLoginGM;
		else if (user.equalsIgnoreCase("justin"))
			return kLoginAdmin;
		else if (user.equalsIgnoreCase("skip"))
			return kLoginPlayer;
		return kLoginInvalid;
	}
	
	boolean setUserSession(HttpServletRequest req, HttpServletResponse res, String user, String hashpass)
	{
		// store the attempted login name for selecting the def. user
		req.getSession().setAttribute(Webspace.LAST_USER, user);
		UserInfo info = new UserInfo();
		info.mUsername = user;
		info.mLevel = validatePermission(user, hashpass);
		
		if (info.mLevel > kLoginInvalid)
		{
			// save the info in our session
			req.getSession().setAttribute(Webspace.CURR_USER, info);
			req.getSession().setMaxInactiveInterval(30*24*60*60); // 30 day
			
			// and save the user logged in the client as a cookie
			res.addCookie(new Cookie(Webspace.CURR_USER, user));
			return true;
		}
		else
		{
			req.getSession().removeAttribute(Webspace.CURR_USER);
			return false;
		}
	}
	
	public void logout(HttpServletRequest req)
	{
		req.getSession().removeAttribute(Webspace.CURR_USER);
		req.getSession().removeAttribute(Webspace.LOGIN_TARGET);
	}
}
