package org.mailnews.helper;

import java.awt.event.ActionListener;
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
    
    private boolean isInitialized = false;

    private List<ActionListener> listeners = new ArrayList<ActionListener>();
    
    public MessageData getMessageData()
    {
        return itsMessageData;
    }

    private MessagesDataSingleton()
    {
        
    }
    
    private void init()
    {
        isInitialized = false;
        if (itsPath == null)
        {
            itsPath = new File("").getAbsolutePath();
        }
        itsMessageData = new MessageData(itsPath);
        defaultLearn(getMessages());
        isInitialized = true;
    }

    public static MessagesDataSingleton getInstance()
    {
        if (instance == null)
        {
            instance = new MessagesDataSingleton();  
        }
        if( !instance.isInitialized)
        {
            instance.init();
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
                        classifier.getClass(Constants.SPAM, msg.getSubject() + " " + Jsoup.parse(msg.getContent()).text(),
                                0.8, "HAM");
                if (!msg.isSpam() && !Constants.SPAM.equals(aClass) || msg.isIgnoreFilter())
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
            if (messageBean.isSpam())
            {
                classifier.learn(messageBean.getMsgId(), Constants.SPAM,
                       messageBean.getSubject() + " " + Jsoup.parse(messageBean.getContent()).text());
            }
            else if (messageBean.isIgnoreFilter())
            {
                classifier.learn(messageBean.getMsgId(), "HAM",
                        messageBean.getSubject() + " " + Jsoup.parse(messageBean.getContent()).text());
            }
        }
        
        classifier.refreshClassifier();
        getWhiteList(messages);
    }

    public boolean isInitialized()
    {
        return isInitialized;
    }

    public void setInitialized(boolean isInitialized)
    {
        this.isInitialized = isInitialized;
        for(ActionListener listener : listeners)
        {
            listener.actionPerformed(null);
        }
        listeners.clear();
    }
    
    public void setListener(ActionListener listener)
    {
        listeners.add(listener);
    }
}
