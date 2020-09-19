package com.ilsian.rmweb;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.LifecycleException;

import com.ilsian.commonweb.res.Loader;
import com.ilsian.tomcat.RedirectServlet;
import com.ilsian.tomcat.StaticResourceServlet;
import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.UserSecurity;
import com.ilsian.tomcat.WebServer;

public class Main {

	static Logger logger = Logger.getLogger("com.embodied.minas.Main");
	
	static private WebServer WEB_SERVER = null;
	
	
	static RMUserSecurity basicSecurity = new RMUserSecurity();
	
	public static void startUp() {
		if (WEB_SERVER == null)
		{
			WEB_SERVER = new WebServer(9990);
			
			// our 'default' servlet, serve static resources and the root favicon
			WEB_SERVER.registerServlet(new StaticResourceServlet(Loader.class).addStaticResourceClass(com.ilsian.rmweb.res.Loader.class), "SRS", new String [] { "/favicon.ico", "/res/*" } );			
			WEB_SERVER.registerServlet(new RMServlet(basicSecurity, "action"), "APP", new String [] { "/gui", "/gui/*" } );
			WEB_SERVER.registerServlet(new RedirectServlet("/gui"), "RDR", new String [] { "/" } );
			
			try {
				WEB_SERVER.startUp();
			} catch (LifecycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void shutDown() {
		if (WEB_SERVER != null)
		{
			try {
				WEB_SERVER.shutDown();
			} catch (LifecycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			WEB_SERVER = null;
		}
	}	
	
	public static void main(String[] args) {
		startUp();
	}

}
