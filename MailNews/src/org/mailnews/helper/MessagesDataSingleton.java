package org.mailnews.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.mailnews.classifier.ClassifierSingleton;
import org.mailnews.properties.Constants;

public class MessagesDataSingleton
{

    private static MessagesDataSingleton instance;

    private MessageData itsMessageData;

    private static String itsPath;

    public MessageData getMessageData()
    {
        return itsMessageData;
    }

    private MessagesDataSingleton()
    {
        if (itsPath == null)
        {
            itsPath = new File("").getAbsolutePath();
        }
        itsMessageData = new MessageData(itsPath);
        defaultLearn(getMessages());
    }

    public static MessagesDataSingleton getInstance()
    {
        if (instance == null)
        {
            instance = new MessagesDataSingleton();  
        }
        return instance;
    }

    public static void setPath(String aPath)
    {
        itsPath = aPath;
    }

    public List<MessageBean> getMessages()
    {
        List<MessageBean> messages = itsMessageData.getMessages();
        return messages;
    }

    public void refreshClassifier()
    {
        defaultLearn(getMessages());
    }
    
    private List<MessageBean> getWhiteList(List<MessageBean> messages)
    {
        List<MessageBean> whiteList = new ArrayList<MessageBean>();
        ClassifierSingleton classifier = ClassifierSingleton.getInstance();
        synchronized (messages)
        {
            for (MessageBean msg : messages)
            {
                String aClass =
                        classifier.getClass(Constants.SPAM, msg.getSubject() + " " + Jsoup.parse(msg.getContent()),
                                0.9, "HAM");
                if (!Constants.SPAM.equals(aClass))
                {
                    whiteList.add(msg);
                }
                else
                {
                    itsMessageData.getSpam().add(messages.get(messages.indexOf(msg)));
                }
            }
            messages.removeAll(itsMessageData.getSpam());
        }
        return whiteList;
    }

    private void defaultLearn(List<MessageBean> messages)
    {
        ClassifierSingleton classifier = ClassifierSingleton.getInstance();
        for (MessageBean messageBean : messages)
        {
            classifier.learn(messageBean.getMsgId(), messageBean.isSpam() ? Constants.SPAM : "HAM",
                    messageBean.getSubject() + " " + Jsoup.parse(messageBean.getContent()));
        }
        getWhiteList(messages);
    }
}
