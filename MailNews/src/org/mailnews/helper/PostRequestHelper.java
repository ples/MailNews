package org.mailnews.helper;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mailnews.properties.AppProperties;
import org.mailnews.properties.Constants;

public class PostRequestHelper 
{
	
	public static void processAdminPostRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		String command = request.getParameter("admin-command");
		if(command.equals("filter-save"))
		{
			saveFilterProperties(request, response);
		}
		if(command.equals("read-speed-save"))
		{
			saveSpeedProperties(request, response);
		}
		if(command.equals("spam-mark"))
		{
			markMailAsSpam(request, response);
		}
		if(command.equals("delete-mail"))
		{
			markMailAsSpam(request, response);
		}
	}
	
	private static void saveFilterProperties(HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		String mailInterval = request.getParameter("mail-life-interval");
		String refreshInterval = request.getParameter("refresh-interval");
		if( mailInterval!= null && refreshInterval!= null)
		{
			try{
				AppProperties.getInstance().setIntProperty(Constants.DAYS_PERIOD,
						Integer.parseInt(mailInterval.trim()));
				AppProperties.getInstance().setIntProperty(Constants.LETTERS_REFRESH_TIME,
						Integer.parseInt(refreshInterval.trim()));
			}
			catch(Exception e)
			{
				response.getWriter().print("fail");
			}
			response.getWriter().print("saved");
		}
	}
	
	private static void saveSpeedProperties(HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		String symbolPerSec = request.getParameter("symb-per-sec");
		String imageWatchTime = request.getParameter("image-watch-time");
		if( symbolPerSec!= null && imageWatchTime!= null)
		{
			try{
				AppProperties.getInstance().setIntProperty(Constants.SYMB_PER_SEC,
						Integer.parseInt(symbolPerSec.trim()));
				AppProperties.getInstance().setIntProperty(Constants.IMAGE_WATCH_TIME_SEC,
						Integer.parseInt(imageWatchTime.trim()));
				
			}
			catch(Exception e)
			{
				response.getWriter().print("fail");
			}
			response.getWriter().print("saved");
		}
	}
	
	private static void markMailAsSpam(HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		
			response.getWriter().print("saved");
		
	}
	
	private static void deleteMails(HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		
		String ids = request.getParameter("selected-mails-id");
		if(ids == null)
		{
			response.getWriter().print("fail");
		}
		List<MessageBean> messages = MessagesDataSingleton.getInstance().getMessageData().getMessages();
		String[] idArr = ids.split(",");
		for (int i = 0; i < idArr.length; i++) 
		{
			for (int j = 0; j < messages.size(); j++) 
			{
				if( messages.get(j).getMsgId() == Integer.parseInt(idArr[i]))
				{
					messages.remove(j);
				}
			}
		}
		response.getWriter().print("saved");
	}
	
	
}
