package org.mailnews.servlet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mailnews.helper.HTMLHelper;
import org.mailnews.helper.MessageBean;
import org.mailnews.helper.MessagesDataSingleton;
import org.mailnews.helper.PostRequestHelper;
import org.mailnews.properties.AppProperties;
import org.mailnews.properties.Constants;

/**
 * Servlet implementation class EmailContentServlet
 */
@WebServlet(name = "EmailContentServlet", urlPatterns = { "/mail/" }, asyncSupported = true)
public class EmailContentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static List<MessageBean> messages;
	private int divWidth;
	private String[] colors;
	private int messageNum = 0;
	private int partNum = 0;
	private int colorNum = 0;
	private String path;
	private String applicationServerURL;
	private String nextUrl;
	private double header_cur_mail_ratio = 15 / (double) 100;
	private boolean initFail = false;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EmailContentServlet() {
		super();
	}

	public void init() throws ServletException {
		
		path = getServletContext().getRealPath("/");
		if(!AppProperties.getInstance().loadFromFile(path+"/props.property"))
		{
			initFail = true;
		}
		
		try {
			MessagesDataSingleton.setPath(path);
			divWidth = AppProperties.getInstance().getIntProperty(Constants.DIV_WIDTH);
			colors = AppProperties.getInstance().getArrayProperty(Constants.COLORS);
			applicationServerURL = "http://"
					+ InetAddress.getLocalHost().getHostAddress()
					+ ":"
					+ getServletContext().getInitParameter(
							"applicationServerURL");
			MessagesDataSingleton.getInstance();
		} catch (Exception e) {
			initFail=true;
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		messages = MessagesDataSingleton.getInstance().getMessages();
		if (messages == null || initFail) {
			response.sendRedirect("error-page.html");
			return;
		}
		if (messages.size() == 0) {
            response.sendRedirect("no-messages.html");
            return;
        }

		if (request.getParameter("next") == null) {
			messageNum = 0;
			partNum = 0;
			colorNum = 0;
		} else {
			try {
				String[] params = request.getParameter("next").split("_");
				colorNum = Integer.parseInt(request.getParameter("color"));
				messageNum = Integer.parseInt(params[0]);
				partNum = Integer.parseInt(params[1]);
				messageNum = messageNum < messages.size() ? messageNum : 0;
				partNum = partNum < messages.get(messageNum).getContentParts().length ? partNum
						: 0;
				colorNum = colorNum < colors.length ? colorNum : 0;
			} catch (Exception e) {
				messageNum = 0;
				partNum = 0;
				colorNum = 0;
			}
		}
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>\n<html>\n").append("<head>\n");
		html.append("<meta charset=\"utf-8\" />");
		html.append("<link rel=\"stylesheet\" href=\"css/style.css\"/>");
		
		html.append("</style>\n")
		.append("<script type=\"text/javascript\" src=\"js/requester.js\"></script>\n")
		.append("<script type=\"text/javascript\" src=\"js/redirect_script.js\"></script>\n")
		.append("</head>\n").append("<body>");
		html.append("<div id=\"main\" style=\"width: ").append(divWidth);
		html.append("px; overflow: hidden;\">");

		addHeaderDiv(html);

		if (messages.get(messageNum).getContentParts().length > 0) {
			html.append(generateProgress(messages.get(messageNum)
					.getContentParts().length, partNum));
			html.append("<div id=\"general-description-text\">");
			html.append(messages.get(messageNum).getContentParts()[partNum]);
			html.append("</div>\n</div>\n</body>\n").append("\n</html>");
		}
		int refreshTime = getTime(messages.get(messageNum).getContentParts()[partNum]);
		nextUrl = getNextUrl();
		StringBuilder meta = new StringBuilder();
		meta.append("<script>\n nextPage(\"").append(nextUrl).append("\",").append(refreshTime)
					.append(");</script>");
		out.println(html.toString() + meta.toString());
		out.close();
	}
	
	private String getNextUrl()
	{
		boolean lastURL = false;
		if (partNum < messages.get(messageNum).getContentParts().length - 1) {
			partNum++;
		} else {
			partNum = 0;
			if (messageNum == messages.size() - 1)
				lastURL = true;
			messageNum = (messageNum + 1) % messages.size();
			colorNum = (colorNum + 1) % colors.length;
		}
		StringBuilder meta = new StringBuilder();

		if (lastURL) {
			meta.append(applicationServerURL);
			lastURL = false;
		} else {
			meta.append(applicationServerURL).append("?next=")
					.append(messageNum).append("_").append(partNum)
					.append("&color=").append(colorNum);
		}
		return meta.toString();
	}

	private String getPrevious()
	{
		boolean lastURL = false;
		if (partNum > 0) {
			partNum--;
		} else {
			if (messageNum == 0)
			{
				lastURL = true;
			} else
			{
				messageNum--;
				partNum = messages.get(messageNum).getContentParts().length-1;
				colorNum = (colorNum + 1) % colors.length;
			}
		}
		StringBuilder meta = new StringBuilder();

		if (lastURL) {
			meta.append(applicationServerURL);
			lastURL = false;
		} else {
			meta.append(applicationServerURL).append("?next=")
					.append(messageNum).append("_").append(partNum)
					.append("&color=").append(colorNum);
		}
		return meta.toString();
	}
	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	private List<AsyncContext> aClientContexts = new ArrayList<AsyncContext>();
	{
		aClientContexts = Collections.synchronizedList(aClientContexts);
	}
	private boolean isStoped = false;
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("client-command") != null)
		{
			AsyncContext aTmpContext = request.startAsync(request, response);
			aTmpContext.addListener(new AsyncListener() {
				
				@Override
				public void onTimeout(AsyncEvent arg0) throws IOException {
					aClientContexts.remove(arg0.getAsyncContext());
				}
				
				@Override
				public void onStartAsync(AsyncEvent arg0) throws IOException {
				}
				
				@Override
				public void onError(AsyncEvent arg0) throws IOException {
					aClientContexts.remove(arg0.getAsyncContext());
				}
				
				@Override
				public void onComplete(AsyncEvent arg0) throws IOException {
					aClientContexts.remove(arg0.getAsyncContext());
				}
			});
			aTmpContext.setTimeout(isStoped ? Integer.MAX_VALUE :Integer.parseInt((request.getParameter("time"))));
			aClientContexts.add(aTmpContext);
		}
		if(request.getParameter("admin-command") != null)
		{
//		    if(!MessagesDataSingleton.getInstance().isInitialized() && 
//		            ("refresh-mails".equals(request.getParameter("admin-command")) 
//		                    || "refresh-spam".equals(request.getParameter("admin-command")) ))
//		    {
//		        AsyncContext aTmpContext = request.startAsync(request, response);
//	            aTmpContext.addListener(new AsyncListener() {
//	                
//	                @Override
//	                public void onTimeout(AsyncEvent arg0) throws IOException {
//	                    anAdminContexts.remove(arg0.getAsyncContext());
//	                }
//	                
//	                @Override
//	                public void onStartAsync(AsyncEvent arg0) throws IOException {
//	                    MessagesDataSingleton.getInstance().setListener(new MyActionListener(arg0.getAsyncContext()));
//	                }
//	                
//	                @Override
//	                public void onError(AsyncEvent arg0) throws IOException {
//	                    anAdminContexts.remove(arg0.getAsyncContext());
//	                }
//	                
//	                @Override
//	                public void onComplete(AsyncEvent arg0) throws IOException {
//	                    anAdminContexts.remove(arg0.getAsyncContext());
//	                }
//	            });
//	            aTmpContext.setTimeout(isStoped ? Integer.MAX_VALUE :Integer.parseInt((request.getParameter("time"))));
//	            aClientContexts.add(aTmpContext);
//		    }
			PostRequestHelper.processAdminPostRequest(request, response);
		}
		String param = request.getParameter("notifier-command");
		if(param==null)
		{
			return;
		}
		if(param.contains("notifier-stop"))
		{
			isStoped = true;
			response.getWriter().print("OK");
			sendCommand("stop");
		}
		if(param.contains("notifier-play"))
		{
			isStoped = false;
			response.getWriter().print("OK");
			sendCommand("play");
		}
		if(param.contains("notifier-prev"))
		{
			response.getWriter().print("OK");
			getPrevious();
			sendCommand("url="+getPrevious());
		}
		if(param.contains("notifier-next"))
		{
			response.getWriter().print("OK");
			sendCommand("url="+nextUrl);
		}
	}
	
	private void sendCommand(String command) throws IOException
	{
		System.out.println(command);
		synchronized (aClientContexts) {
			for(AsyncContext aClientContext : aClientContexts)
			{
				aClientContext.getResponse().getWriter().print(command);
				aClientContext.getResponse().getWriter().flush();
				aClientContext.complete();
			}
		}
		
	}

	private int getTime(String div) {
		int indFst = div.indexOf(HTMLHelper.STR_LEN_ATTR)
				+ HTMLHelper.STR_LEN_ATTR.length() + 2;
		int length = Integer.parseInt(div.substring(indFst,
				div.indexOf("\"", indFst + 1)));
		return length / AppProperties.getInstance().getIntProperty(Constants.SYMB_PER_SEC);
	}

	private String generateProgress(int pagesCount, int currentPage) {
		String div = "<div id=\"progress\" >\n";
		for (int i = 0; i < pagesCount; i++) {
			if (i == currentPage) {
				div += "<a class=\"active\"></a>\n";
			} else {
				div += "<a ></a>\n";
			}
		}

		div += "</div>";
		return div;
	}

	private void addHeaderDiv(StringBuilder html) {
		html.append("<table id='tab-head'><tr id=\"header-text\"><td style=\"width:"
				+ (int) (divWidth * (1 - header_cur_mail_ratio)) + "px;background-color:").
				append(colors[colorNum]).append("\">");
		html.append("<div  ")
				.append(">");
		html.append(messages.get(messageNum).getSubject());
		html.append("</div>");
		html.append(
				"</td><td style=\"width:"
						+ (int) (divWidth * header_cur_mail_ratio)
						+ "px; overflow:hidden; background-color:")
				.append(colors[(colorNum + 1) % colors.length]).append(";\">");
		addDivCurrentMail(html);
		html.append("</td></tr></table>");
	}

	private void addDivCurrentMail(StringBuilder html) {
		html
				.append("<img src=\"img/mail_new.png\">")
				.append("<div>")
				.append((messageNum + 1) + "\\" + messages.size())
				.append("</div>");
	}
	
	class MyActionListener implements ActionListener
    {
        
        private AsyncContext ctx;
        
        public MyActionListener(AsyncContext aCtx)
        {
            super();
            this.ctx = aCtx;
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                PostRequestHelper.processAdminPostRequest((HttpServletRequest)ctx.getRequest(), (HttpServletResponse)ctx.getResponse());
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

}
