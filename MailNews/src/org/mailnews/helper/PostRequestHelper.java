package org.mailnews.helper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PostRequestHelper 
{
	
	public static void processAdminPostRequest(HttpServletRequest request,
			HttpServletResponse response)
	{
		String command = request.getParameter("admin-command");
		if(command.equals("filter-save"))
		{
			saveFilterProperties(request, response);
		}
		if(command.equals("filter-save"))
		{
			saveFilterProperties(request, response);
		}
	}
	
	private static void saveFilterProperties(HttpServletRequest request,
			HttpServletResponse response)
	{
		
	}
}
