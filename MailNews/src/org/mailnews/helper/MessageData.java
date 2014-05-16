package org.mailnews.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mailnews.properties.AppProperties;
import org.mailnews.properties.Constants;

public class MessageData
{

    private static volatile List<MessageBean> messages;
    private static volatile List<MessageBean> spam;
    HTMLHelper htmlHelper;
    private String path = "";
    private static boolean checkNewMessages = false;
    private static volatile boolean updateInProgress = false;
    private static volatile long lastUpdate;

    public MessageData(String aPath)
    {
        path = aPath;
        spam = new ArrayList<MessageBean>();
        initMessages();
        checkNewMessages = true;
    }

    private void initMessages()
    {
        if (null == messages || messages.size() == 0)
        {
            checkNewMessages = false;
        }
        else
        {
            checkNewMessages = true;
        }
        if (updateInProgress)
        {
            return;
        }
        updateInProgress = true;
        try
        {
            AppProperties myProps = AppProperties.getInstance();
            List<MessageBean> currentMessages =
                    MailHelper.receiveMessage(myProps.getProperty(Constants.LOGIN),
                            myProps.getProperty(Constants.PASSWORD), myProps.getProperty(Constants.HOST),
                            myProps.getProperty(Constants.PORT), myProps.getProperty(Constants.STORE_PROTOCOL), path,
                            checkNewMessages);
            if (currentMessages.size() == 0)
            {
                updateInProgress = false;
                return;
            }
            htmlHelper =
                    new HTMLHelper(myProps.getIntProperty(Constants.FONT_SIZE),
                            myProps.getIntProperty(Constants.SPACING),
                            myProps.getIntProperty(Constants.FONT_HEADER_SIZE),
                            myProps.getIntProperty(Constants.HEADER_SPACING),
                            myProps.getProperty(Constants.FONT_FAMILY),
                            myProps.getProperty(Constants.HEADER_FONT_FAMILY),
                            myProps.getIntArrayProperty(Constants.TEXT_MARGIN)[Constants.TOP],
                            myProps.getIntProperty(Constants.IMG_MARGIN), path,
                            myProps.getIntProperty(Constants.TEXT_INDENTER));
            for (int i = 0; i < currentMessages.size(); i++)
            {
                String subject = currentMessages.get(i).getSubject();
                String content = currentMessages.get(i).getContent();
                htmlHelper.setDivHeight(myProps.getIntProperty(Constants.DIV_HEIGHT));
                htmlHelper.setDivWidth(myProps.getIntProperty(Constants.DIV_HEIGHT));
                int headerH =
                        htmlHelper.getHeaderHeight(subject, myProps.getIntArrayProperty(Constants.HEADER_MARGIN), 0);
                htmlHelper.setDivHeight(myProps.getIntProperty(Constants.DIV_HEIGHT) - headerH
                        - myProps.getIntProperty(Constants.PROGRESS_BAR_HEIGHT));
                htmlHelper.setDivWidth(myProps.getIntProperty(Constants.DIV_WIDTH)
                        - myProps.getIntArrayProperty(Constants.TEXT_MARGIN)[Constants.LEFT] * 2);
                currentMessages.get(i).setContentParts(
                        htmlHelper.splitContent(content, currentMessages.get(i).getAttachments(), currentMessages
                                .get(i).getEncoding().toLowerCase().contains("plain") ? "plain" : "mixed"));
            }
            if (checkNewMessages)
            {
                messages.addAll(currentMessages);
                if(currentMessages.size() > 0)
                    MessagesDataSingleton.getInstance().refreshClassifier();
            }
            else
            {
                messages = currentMessages;
            }
            messages = MailHelper.filterMessages(messages);
            messages = Collections.synchronizedList(new ArrayList<MessageBean>(messages));
            checkNewMessages = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        lastUpdate = System.currentTimeMillis();
        updateInProgress = false;
    }

    private void checkForUpdate()
    {
        System.out.println(AppProperties.getInstance().getIntProperty(
                Constants.LETTERS_REFRESH_TIME));
        if (System.currentTimeMillis() - lastUpdate > AppProperties.getInstance().getIntProperty(
                Constants.LETTERS_REFRESH_TIME) * 60000
                && !updateInProgress)
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    initMessages();
                }
            });
            thread.start();
        }
    }

    public List<MessageBean> getMessages()
    {
        if(messages == null)
        {
            messages = new ArrayList<MessageBean>();
        }
        checkForUpdate();
        return messages;
    }

    public List<MessageBean> getSpam()
    {
        return spam;
    }
}
