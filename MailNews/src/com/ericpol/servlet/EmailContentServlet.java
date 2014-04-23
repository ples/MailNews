package com.ericpol.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ericpol.helper.HTMLHelper;
import com.ericpol.helper.MessageBean;
import com.ericpol.helper.MessageData;

/**
 * Servlet implementation class EmailContentServlet
 */
public class EmailContentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static List<MessageBean> messages;
	private static MessageData messageData;
	private int divWidth = 1920;
	private String[] colors = { "#355d7f", "#22aa88", "#882255", "#fbaf22" };

	public final static int symbPerSec = 50;
	private int messageNum = 0;
	private int partNum = 0;
	private int colorNum = 0;
	private String path;
	private String applicationServerURL;
	private double header_cur_mail_ratio = 15/(double)100;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EmailContentServlet() {
		super();
	}

	public void init() throws ServletException {
		path = getServletContext().getRealPath("/");
		messageData = new MessageData(path);
		try {
			applicationServerURL = "http://"
					+ InetAddress.getLocalHost().getHostAddress()
					+ ":"
					+ getServletContext().getInitParameter(
							"applicationServerURL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		messages = messageData.getMessages();
		if (messages == null || messages.size() == 0) {
			response.sendRedirect(applicationServerURL);
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
		html.append("<link rel=\"stylesheet\" href=\"style.css\"/>");
		html.append("</style>\n").append("</head>\n").append("<body>");
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
			meta.append("<meta http-equiv=\"refresh\" content=\"").append(3)
					.append(";").append(applicationServerURL).append("\">");
			lastURL = false;
		} else {
			meta.append("<meta http-equiv=\"refresh\" content=\"").append(3)
					.append(";?next=").append(messageNum).append("_")
					.append(partNum).append("&color=").append(colorNum)
					.append("\">");
		}

		out.println(html.toString() + meta.toString());
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private int getTime(String div) {
		int indFst = div.indexOf(HTMLHelper.STR_LEN_ATTR)
				+ HTMLHelper.STR_LEN_ATTR.length() + 2;
		int length = Integer.parseInt(div.substring(indFst,
				div.indexOf("\"", indFst + 1)));
		return length / symbPerSec;
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
		html.append("<table id='tab-head'><tr><td style=\"width:"
				+ (int) (divWidth * (1 - header_cur_mail_ratio)) + "px;\">");
		html.append("<div id=\"header-text\" style=\"background-color:")
				.append(colors[colorNum]).append("\">");
		html.append(messages.get(messageNum).getSubject());
		html.append("</div>");
		html.append(
				"</td><td style=\"width:" + (int) (divWidth * header_cur_mail_ratio)
						+ "px; overflow:hidden; background-color:")
				.append(colors[(colorNum + 1) % colors.length]).append(";\">");
		addDivCurrentMail(html);
		html.append("</td></tr></table>");
	}

	private void addDivCurrentMail(StringBuilder html) {
		html.append("<div id='current_mail'>")
				.append("<img src=\"mail_new.png\">")
				.append("<div id=\"header-text\" >")
				.append((messageNum + 1) + "\\" + messages.size())
				.append("</div></div>");
	}

}
