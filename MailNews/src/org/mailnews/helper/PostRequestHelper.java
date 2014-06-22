package org.mailnews.helper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mailnews.classifier.ClassifierSingleton;
import org.mailnews.properties.AppProperties;
import org.mailnews.properties.Constants;

public class PostRequestHelper
{
    
    public static void processAdminPostRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        String command = request.getParameter("admin-command");
        if (command.equals("filter-save"))
        {
            saveFilterProperties(request, response);
        }
        if (command.equals("read-speed-save"))
        {
            saveSpeedProperties(request, response);
        }
        if (command.equals("spam-mark"))
        {
            markMailAsSpam(request, response);
        }
        if (command.equals("delete-mail"))
        {
            deleteMails(request, response);
        }
        if (command.equals("delete-mark"))
        {
            deleteSpamMark(request, response);
        }
        if (command.equals("delete-spam"))
        {
            deleteMails(request, response);
        }
        if (command.equals("refresh-mails"))
        {
            refresh(request, response);
        }
        if (command.equals("refresh-spam"))
        {
            refresh(request, response);
        }
        if (command.equals("classifier-save"))
        {
            saveClassifierDictionary(request, response);
        }
    }

    public static void processIdentifierCommand(HttpServletRequest request, HttpServletResponse response, Map<String, SessionProperties> aSessions) throws IOException
    {
        String name = request.getParameter("connection-name");
        for (SessionProperties sessionProperties : aSessions.values())
        {
            if(name.equals(sessionProperties.getName()))
            {
                response.getWriter().print("not-free");
                response.getWriter().close();
                return;
            }
        }
        response.getWriter().print("ok");
        aSessions.put(name ,new SessionProperties(name, null));
    }
    
    private static void saveClassifierDictionary(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setCharacterEncoding("UTF-8");
        String newWords = request.getParameter("new-words");
        if( null != newWords )
        {
            if ("spam".equals(request.getParameter("dictionary-class")))
            {
                ClassifierSingleton.getInstance().setCustomSpamDictionary(newWords);
            }
            else
            {
                ClassifierSingleton.getInstance().setCustomHamDictionary(newWords);
            }
            MessagesDataSingleton.getInstance().refreshClassifier();
            response.getWriter().print("saved");
        }
            else
        {
            response.getWriter().print("fail");
        }
    }
    
    
    private static void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setCharacterEncoding("UTF-8");
        List<MessageBean> messages;
        if ("refresh-mails".equals(request.getParameter("admin-command")))
        {
            messages = MessagesDataSingleton.getInstance().getMessages();
        }
        else
        {
            messages = MessagesDataSingleton.getInstance().getMessageData().getSpam();
        }
        StringBuilder html = new StringBuilder("refresh-result:");
        synchronized (messages)
        {
            html.append("#*separator*#");
            for (MessageBean eachMessage : messages)
            {
                html.append("<li id=\"").append(eachMessage.getMsgId())
                        .append("\" class=\"ui-widget-content\">");
                html.append(eachMessage.getSubject().replaceAll("#*separator*#", "*separator*"));
                html.append("</li>");
            }
            html.append("#*separator*#");
            for (MessageBean eachMessage : messages)
            {
                html.append("<div id=\"").append(eachMessage.getMsgId())
                        .append("-content\" style=\"display: none;\" class=\"ui-widget-content\">");
                html.append(eachMessage.getContent().replaceAll("#*separator*#", "*separator*"));

                if (eachMessage.getAttachments() != null)
                    for (String attachment : eachMessage.getAttachments())
                    {
                        html.append("<img alt=\"Image\" src=\"").append(attachment).append("\">");
                    }
                html.append("</div>");
            }
        }
        response.getWriter().print(html.toString());
        response.getWriter().close();
    }

    private static void saveFilterProperties(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        String mailInterval = request.getParameter("mail-life-interval");
        String refreshInterval = request.getParameter("refresh-interval");
        if (mailInterval != null && refreshInterval != null)
        {
            try
            {
                AppProperties.getInstance().setIntProperty(Constants.DAYS_PERIOD,
                        Integer.parseInt(mailInterval.trim()));
                AppProperties.getInstance().setIntProperty(Constants.LETTERS_REFRESH_TIME,
                        Integer.parseInt(refreshInterval.trim()));
            }
            catch (Exception e)
            {
                response.getWriter().print("fail");
            }
            response.getWriter().print("saved");
        }
    }

    private static void saveSpeedProperties(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        String symbolPerSec = request.getParameter("symb-per-sec");
        String imageWatchTime = request.getParameter("image-watch-time");
        if (symbolPerSec != null && imageWatchTime != null)
        {
            try
            {
                AppProperties.getInstance().setIntProperty(Constants.SYMB_PER_SEC,
                        Integer.parseInt(symbolPerSec.trim()));
                AppProperties.getInstance().setIntProperty(Constants.IMAGE_WATCH_TIME_SEC,
                        Integer.parseInt(imageWatchTime.trim()));

            }
            catch (Exception e)
            {
                response.getWriter().print("fail");
            }
            response.getWriter().print("saved");
        }
    }

    private static void markMailAsSpam(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try
        {
            String ids = request.getParameter("selected-mails-id");
            List<MessageBean> messages = MessagesDataSingleton.getInstance().getMessageData().getMessages();
            String[] idArr = ids.split(",");
            for (int i = 0; i < idArr.length; i++)
            {
                for (int j = 0; j < messages.size(); j++)
                {
                    if (messages.get(j).getMsgId() == Integer.parseInt(idArr[i]))
                    {
                        messages.get(j).setSpam(true);
                        messages.get(j).setIgnoreFilter(false);
                        break;
                    }
                }
            }
            MessagesDataSingleton.getInstance().refreshClassifier();
            List<MessageBean> spam = MessagesDataSingleton.getInstance().getMessageData().getSpam();
            String spamIds = "";
            for (MessageBean messageBean : spam)
            {
                spamIds += messageBean.getMsgId() + ",";
            }
            spamIds = spamIds.substring(0, spamIds.length() - 1);
            response.getWriter().print("saved :" + spamIds);
        }
        catch (Exception e)
        {
            response.getWriter().print("fail");
        }
    }

    private static void deleteMails(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try
        {
            String ids;
            List<MessageBean> messages;
            if (!"delete-spam".equals(request.getParameter("admin-command")))
            {
                messages = MessagesDataSingleton.getInstance().getMessageData().getMessages();
                ids = request.getParameter("selected-mails-id");
            }
            else
            {
                messages = MessagesDataSingleton.getInstance().getMessageData().getSpam();
                ids = request.getParameter("selected-spam-id");
            }

            String[] idArr = ids.split(",");
            for (int i = 0; i < idArr.length; i++)
            {
                for (int j = 0; j < messages.size(); j++)
                {
                    if (messages.get(j).getMsgId() == Integer.parseInt(idArr[i]))
                    {
                        messages.remove(j);
                        break;
                    }
                }
            }
            response.getWriter().print("saved deleted");
        }
        catch (Exception e)
        {
            response.getWriter().print("fail");
        }
    }

    private static void deleteSpamMark(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try
        {
            String ids = request.getParameter("selected-spam-id");
            List<MessageBean> messages = MessagesDataSingleton.getInstance().getMessageData().getMessages();
            List<MessageBean> spam = MessagesDataSingleton.getInstance().getMessageData().getSpam();
            String[] idArr = ids.split(",");
            for (int i = 0; i < idArr.length; i++)
            {
                for (int j = 0; j < spam.size(); j++)
                {
                    if (spam.get(j).getMsgId() == Integer.parseInt(idArr[i]))
                    {
                        spam.get(j).setIgnoreFilter(true);
                        messages.add(spam.remove(j));
                        break;
                    }
                }
            }
            MessagesDataSingleton.getInstance().refreshClassifier();
            String msgIds = "";
            for (MessageBean messageBean : messages)
            {
                msgIds += messageBean.getMsgId() + ",";
            }
            msgIds = msgIds.substring(0, msgIds.length() - 1);
            response.getWriter().print("saved :" + msgIds);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            response.getWriter().print("fail");
        }
    }
}
