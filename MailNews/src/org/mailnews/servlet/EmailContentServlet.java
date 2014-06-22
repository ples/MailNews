package org.mailnews.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mailnews.helper.CommandSender;
import org.mailnews.helper.HTMLHelper;
import org.mailnews.helper.MessageBean;
import org.mailnews.helper.MessagesDataSingleton;
import org.mailnews.helper.PostRequestHelper;
import org.mailnews.helper.SessionProperties;
import org.mailnews.properties.AppProperties;
import org.mailnews.properties.Constants;

@WebServlet(name = "EmailContentServlet", urlPatterns = {"/mail/"}, asyncSupported = true)
public class EmailContentServlet extends HttpServlet
{
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
    private static volatile Map<String, SessionProperties> sessions = Collections
            .synchronizedMap(new HashMap<String, SessionProperties>());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmailContentServlet()
    {
        super();
    }

    public void init() throws ServletException
    {
        path = getServletContext().getRealPath("/");
        if (!AppProperties.getInstance().loadFromFile(path + "/props.property"))
        {
            initFail = true;
        }

        try
        {
            MessagesDataSingleton.setPath(path);
            divWidth = AppProperties.getInstance().getIntProperty(Constants.DIV_WIDTH);
            colors = AppProperties.getInstance().getArrayProperty(Constants.COLORS);
            applicationServerURL =
                    "http://" + InetAddress.getLocalHost().getHostAddress() + ":"
                            + getServletContext().getInitParameter("applicationServerURL");
            MessagesDataSingleton.getInstance();
        }
        catch (Exception e)
        {
            initFail = true;
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        String myClientName = request.getParameter("name");
        messages = MessagesDataSingleton.getInstance().getMessages();
        if (messages == null || initFail)
        {
            response.sendRedirect("error-page.html");
            return;
        }
        if (messages.size() == 0)
        {
            response.sendRedirect("no-messages.html");
            return;
        }

        if (null == myClientName || null == sessions.get(myClientName))
        {
            response.sendRedirect("identifier.html");
            return;
        }
        if (request.getParameter("next") == null)
        {
            messageNum = 0;
            partNum = 0;
            colorNum = 0;
        }
        else
        {
            try
            {
                String[] params = request.getParameter("next").split("_");
                colorNum = Integer.parseInt(request.getParameter("color"));
                messageNum = Integer.parseInt(params[0]);
                partNum = Integer.parseInt(params[1]);
                messageNum = messageNum < messages.size() ? messageNum : 0;
                partNum = partNum < messages.get(messageNum).getContentParts().length ? partNum : 0;
                colorNum = colorNum < colors.length ? colorNum : 0;
            }
            catch (Exception e)
            {
                messageNum = 0;
                partNum = 0;
                colorNum = 0;
            }
        }
        SessionProperties currentSessionProperties = sessions.get(myClientName);
        currentSessionProperties.setCurrentMail(messageNum);
        currentSessionProperties.setCurrentPage(partNum);
        currentSessionProperties.setCurrentColor(colorNum);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n").append("<head>\n");
        html.append("<meta charset=\"utf-8\" />");
        html.append("<link rel=\"stylesheet\" href=\"css/style.css\"/>");

        html.append("</style>\n").append("<script type=\"text/javascript\" src=\"js/requester.js\"></script>\n")
                .append("<script type=\"text/javascript\" src=\"js/redirect_script.js\"></script>\n")
                .append("</head>\n").append("<body>");
        html.append("<div id=\"main\" style=\"width: ").append(divWidth);
        html.append("px; overflow: hidden;\">");

        addHeaderDiv(html);

        if (messages.get(messageNum).getContentParts().length > 0)
        {
            html.append(generateProgress(messages.get(messageNum).getContentParts().length, partNum));
            html.append("<div class=\"device-info\">").append(currentSessionProperties.getName()).append("</div>");
            html.append("<div id=\"general-description-text\">");
            html.append(messages.get(messageNum).getContentParts()[partNum]);
            html.append("</div>\n</div>\n</body>\n").append("\n</html>");
        }
        html.append("<intput id=\"client-name-id\" type=\"hidden\" value=\"" + currentSessionProperties.getName()
                + "\" />\n");
        int refreshTime = getTime(messages.get(messageNum).getContentParts()[partNum]);
        nextUrl = getNextUrl(currentSessionProperties);
        StringBuilder meta = new StringBuilder();
        meta.append("<script>\n nextPage(\"").append(nextUrl).append("\",").append(refreshTime).append(");</script>");
        out.println(html.toString() + meta.toString());
        out.close();
    }

    private String getNextUrl(SessionProperties client)
    {
        int myMailNum = client.getCurrentMail();
        int myPageNum = client.getCurrentPage();
        int myColorNum = client.getCurrentColor();
        boolean lastURL = false;
        if (myPageNum < messages.get(myMailNum).getContentParts().length - 1)
        {
            myPageNum++;
        }
        else
        {
            myPageNum = 0;
            if (myMailNum == messages.size() - 1)
                lastURL = true;
            myMailNum = (myMailNum + 1) % messages.size();
            myColorNum = (myColorNum + 1) % colors.length;
        }
        StringBuilder meta = new StringBuilder();

        if (lastURL)
        {
            myMailNum = 0;
            myPageNum = 0;
            myColorNum = 0;
            lastURL = false;
        }

        meta.append(applicationServerURL).append("?name=").append(client.getName()).append("&next=")
                .append(myMailNum).append("_").append(myPageNum).append("&color=").append(myColorNum);

        return meta.toString();
    }

    private String getPrevious(SessionProperties client)
    {
        int myMailNum = client.getCurrentMail();
        int myPageNum = client.getCurrentPage();
        int myColorNum = client.getCurrentColor();
        boolean lastURL = false;
        if (myPageNum > 0)
        {
            myPageNum--;
        }
        else
        {
            if (myMailNum == 0)
            {
                lastURL = true;
            }
            else
            {
                myMailNum--;
                myPageNum = messages.get(myMailNum).getContentParts().length - 1;
                myColorNum = (myColorNum + 1) % colors.length;
            }
        }
        StringBuilder meta = new StringBuilder();

        if (lastURL)
        {
            myMailNum = messages.size() - 1;
            myPageNum = messages.get(myMailNum).getContentParts().length - 1;
            lastURL = false;
        }
        meta.append(applicationServerURL).append("?name=").append(client.getName()).append("&next=")
                .append(myMailNum).append("_").append(myPageNum).append("&color=").append(myColorNum);
        return meta.toString();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */

    private boolean isStoped = false;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        if (request.getParameter("client-command") != null)
        {
            String myClientName = request.getParameter("client-name");
            if (myClientName == null)
                return;
            AsyncContext aTmpContext = request.startAsync(request, response);
            aTmpContext.addListener(new AsyncListener()
            {

                @Override
                public void onTimeout(AsyncEvent arg0) throws IOException
                {
                    
                }

                @Override
                public void onStartAsync(AsyncEvent arg0) throws IOException
                {
                }

                @Override
                public void onError(AsyncEvent arg0) throws IOException
                {

                }

                @Override
                public void onComplete(AsyncEvent arg0) throws IOException
                {
                    
                }
            });
            aTmpContext.setTimeout(isStoped ? Integer.MAX_VALUE
                    : 2 * Integer.parseInt((request.getParameter("time"))));
            synchronized (sessions)
            {
                System.out.println("CREATE: " + aTmpContext);
                sessions.get(myClientName).setItsContext(aTmpContext);
            }
        }
        if (request.getParameter("admin-command") != null)
        {

            PostRequestHelper.processAdminPostRequest(request, response);
        }
        if (null != request.getParameter("connection-name"))
        {
            PostRequestHelper.processIdentifierCommand(request, response, sessions);
        }
        String param = request.getParameter("notifier-command");
        String myClientName = request.getParameter("client-name");
        if (param == null || myClientName == null)
        {
            return;
        }
        if (param.contains("notifier-stop"))
        {
            isStoped = true;
            response.getWriter().print("OK");
            sendCommand(myClientName, "stop");
        }
        if (param.contains("notifier-play"))
        {
            isStoped = false;
            response.getWriter().print("OK");
            sendCommand(myClientName, "play");
        }
        if (param.contains("notifier-prev"))
        {
            response.getWriter().print("OK");
            getPrevious(sessions.get(myClientName));
            sendCommand(myClientName, "url=" + getPrevious(sessions.get(myClientName)));
        }
        if (param.contains("notifier-next"))
        {
            response.getWriter().print("OK");
            sendCommand(myClientName, "url=" + getNextUrl(sessions.get(myClientName)));
        }
    }

    private void sendCommand(String clientName, String command) throws IOException
    {
        CommandSender sender = new CommandSender()
        {
            String clientName;
            String command;

            @Override
            public void setCommand(String aCommand)
            {
                command = aCommand;
            }

            @Override
            public void setClient(String aClient)
            {
                clientName = aClient;
            }
            @Override
            public void run()
            {
                SessionProperties client;
                boolean success = false;

                while (!success)
                {
                    synchronized (sessions)
                    {
                        client = sessions.get(clientName);
                    }
                    AsyncContext currentContext = client.getItsContext();
                    synchronized (currentContext)
                    {
                        System.out.println(currentContext);
                        try
                        {
                            try
                            {
                                currentContext.getResponse().getWriter().print(command);
                                currentContext.getResponse().getWriter().flush();
                                currentContext.complete();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            success = true;
                        }
                        catch (IllegalStateException e)
                        {
                            try
                            {
                                System.out.println("IllegalState");
                                Thread.sleep(20);
                            }
                            catch (InterruptedException e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }

        };
        Thread senderThread = new Thread(sender);
        sender.setClient(clientName);
        sender.setCommand(command);
        senderThread.start();
    }

    private int getTime(String div)
    {
        int indFst = div.indexOf(HTMLHelper.STR_LEN_ATTR) + HTMLHelper.STR_LEN_ATTR.length() + 2;
        int length = Integer.parseInt(div.substring(indFst, div.indexOf("\"", indFst + 1)));
        return length / AppProperties.getInstance().getIntProperty(Constants.SYMB_PER_SEC);
    }

    private String generateProgress(int pagesCount, int currentPage)
    {
        String div = "<div id=\"progress\" >\n";
        for (int i = 0; i < pagesCount; i++)
        {
            if (i == currentPage)
            {
                div += "<a class=\"active\"></a>\n";
            }
            else
            {
                div += "<a ></a>\n";
            }
        }
        div += "</div>";
        return div;
    }

    private void addHeaderDiv(StringBuilder html)
    {
        html.append(
                "<table id='tab-head'><tr id=\"header-text\"><td style=\"width:"
                        + (int) (divWidth * (1 - header_cur_mail_ratio)) + "px;background-color:")
                .append(colors[colorNum]).append("\">");
        html.append(messages.get(messageNum).getSubject());
        html.append(
                "</td><td style=\"width:" + (int) (divWidth * header_cur_mail_ratio)
                        + "px; overflow:hidden; background-color:").append(colors[(colorNum + 1) % colors.length])
                .append(";\">");
        addDivCurrentMail(html);
        html.append("</td></tr></table>");
    }

    private void addDivCurrentMail(StringBuilder html)
    {
        html.append("<img src=\"img/mail_new.png\">").append((messageNum + 1) + "\\" + messages.size());
    }

    public static Map<String, SessionProperties> getSessions()
    {
        return sessions;
    }

}
