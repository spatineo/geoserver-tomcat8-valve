package org.geoserver.tomcat;

import java.io.CharArrayWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class GeoserverTomcatAccessLogValve extends AccessLogValve {
	private static final Log log = LogFactory.getLog(GeoserverTomcatAccessLogValve.class);
	
	@Override
	protected AccessLogElement createAccessLogElement(String name, char pattern) {
		if (pattern == 'G') {
			if ("geoserver-user".equals(name)) {
				return this.new GeoserverUserElement();
			}
			if ("geoserver-or-normal-user".equals(name)) {
				return this.new GeoserverUserOrNormalUserElement();
			}
		}

		return super.createAccessLogElement(name, pattern);
	}
	
	protected class GeoserverUserElement implements AccessLogElement {
		
		public void addElement(CharArrayWriter buf, Date date, Request request, Response response, long time) {
			String username = null;
			
			HttpSession session = request.getSession(false);
			
			if (session != null) {
				username = extractUsernameFromSession(session);
			}

			if (username != null) {
				buf.append(username);
			} else {
				buf.append("-");
			}
		}

	}
	
	protected class GeoserverUserOrNormalUserElement extends UserElement {
		public void addElement(CharArrayWriter buf, Date date, Request request, Response response, long time) {
			String username = null;
			
			HttpSession session = request.getSession(false);
			
			if (session != null) {
				username = extractUsernameFromSession(session);
			}

			if (username != null) {
				buf.append(username);
			} else {
				super.addElement(buf, date, request, response, time);
			}
		}
	}

	private String extractUsernameFromSession(HttpSession session) {
		try {
			Object springSecurityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
			
			if (springSecurityContext != null) {
				return extractUsernameFromSpringSecurityContext(springSecurityContext);
			}
			
		} catch(Exception e) {
			log.error("Error determining geoserver username from HttpSession", e);
		}
		return null;
	}

	private String extractUsernameFromSpringSecurityContext(Object springSecurityContext) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		Method getAuthentication = springSecurityContext.getClass().getMethod("getAuthentication");
		Object authentication = getAuthentication.invoke(springSecurityContext);
		
		if (authentication != null) {					
			return extractUsernameFromAuthentication(authentication);
		}
		return null;
	}

	private String extractUsernameFromAuthentication(Object authentication) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		Method getPrincipal = authentication.getClass().getMethod("getPrincipal");
		Object principal = getPrincipal.invoke(authentication);
		
		if (principal != null) {
			return extractUsernameFromPrincipal(principal);
		}
		return null;
	}

	private String extractUsernameFromPrincipal(Object principal) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		Method getUsername = principal.getClass().getMethod("getUsername");
		Object tmp = getUsername.invoke(principal);
		if (tmp != null) {
			String username = tmp.toString();
			if (log.isTraceEnabled()) {
				log.trace("Found geoserver username '"+username+"'");
			}
			return username;
		}

		return null;
	}
}
