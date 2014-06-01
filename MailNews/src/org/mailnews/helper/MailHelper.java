package org.mailnews.helper;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.FlagTerm;

import org.mailnews.properties.AppProperties;
import org.mailnews.properties.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class MailHelper
{

    static final String ENCODING = "UTF-8";

    static private String PATH = null;
    static private AtomicInteger attachmentId = new AtomicInteger(0);
    static private String[] attachmentFormats = {"jpg", "jpeg", "png", "gif"};
    
    static private Map<String, String> tempFileMap = new HashMap<String, String>(); 

    public static ArrayList<MessageBean> receiveMessage(String user, String password, String host, String port,
            String storeProtocol, String path, boolean checkNewMessages) throws Exception
    {
        PATH = path;

        Properties props = new Properties();

        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", port);
        props.setProperty("mail.store.protocol", storeProtocol);

        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore(storeProtocol);
        store.connect(host, user, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        // inbox.open(Folder.READ_ONLY);
        Message[] messages;

        if (checkNewMessages)
        {
            messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        }
        else
        {
            messages = inbox.getMessages();
        }
        messages = filterMessagesByDate(messages);
        if (messages == null)
        {
            return new ArrayList<MessageBean>();
        }

        String[] msgs = new String[messages.length];
        for (int i = 0; i < messages.length; i++)
        {
            msgs[i] = messages[i].getSubject();
        }

        ArrayList<MessageBean> listMessages = getPart(messages);

        inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);

        inbox.close(false);
        store.close();

        return listMessages;
    }

    private static ArrayList<MessageBean> getPart(Message[] messages) throws Exception
    {
        ArrayList<MessageBean> listMessages = new ArrayList<MessageBean>();
        for (Message inMessage : messages)
        {
            tempFileMap.clear();
            try
            {
                List<String> attachments = null;
                if (inMessage.isMimeType("text/*"))
                {
                    MessageBean message =
                            new MessageBean(inMessage.getMessageNumber(),
                                    MimeUtility.decodeText(checkForNull(inMessage.getSubject())),
                                    inMessage.getFrom()[0].toString(), null, inMessage.getReceivedDate(),
                                    (String) inMessage.getContent(), false, null, inMessage.getContentType());
                    listMessages.add(message);
                }
                else if (inMessage.isMimeType("multipart/*"))
                {
                    Multipart mp = (Multipart) inMessage.getContent();

                    MessageBean message =
                            new MessageBean(inMessage.getMessageNumber(),
                                    MimeUtility.decodeText(checkForNull(inMessage.getSubject())),
                                    getFrom(inMessage.getFrom()), null, inMessage.getReceivedDate(), null, false,
                                    null, inMessage.getContentType());
                    processMultipartBody(mp, message, attachments);
                    for(String eachFile : tempFileMap.keySet())
                    {
                        message.setContent(message.getContent().replaceAll(eachFile, tempFileMap.get(eachFile)));
                    }
                    listMessages.add(message);
                }
            }
            catch (Exception e)
            {
                
            }
        }

        return listMessages;
    }

    private static String getFrom(Address[] from)
    {
        if (from != null)
            if (from.length > 0)
                return from[0].toString();
        return "UNKNOWN";
    }

    private static void processMultipartBody(Multipart mp, MessageBean message, List<String> attachments)
            throws Exception
    {

        for (int i = 0; i < mp.getCount(); i++)
        {
            Part part = mp.getBodyPart(i);
            if ((part.getFileName() == null || "".equals(part.getFileName())) && (part.isMimeType("text/*")))
            {
                message.setContent((String) part.getContent());
                message.setEncoding(part.getContentType());
            }
            else if (part.getFileName() != null && !"".equals(part.getFileName()))
            {
                if ((part.getDisposition() != null) && (part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)))
                {
                    if (attachments == null)
                        attachments = new ArrayList<String>();
                    String newFile = saveFile(MimeUtility.decodeText(part.getFileName()), part.getInputStream());
                    tempFileMap.put(MimeUtility.decodeText(part.getFileName()), newFile);
                    if (newFile == null)
                    {
                        continue;
                    }
                    attachments.add(newFile);
                    message.setAttachments(attachments);
                }
            }
            else if (part.isMimeType("multipart/*"))
            {
                processMultipartBody((Multipart) part.getContent(), message, attachments);
            }
        }
    }

    private static String saveFile(String filename, InputStream input)
    {
        String format = getFormat(filename.toLowerCase());
        if (format == null)
        {
            return null;
        }
        String outputName = attachmentId.incrementAndGet() + "." + format;
        String path = PATH + outputName;
        File file = new File(path);

        try
        {
            FileOutputStream out = new FileOutputStream(file);
            int b = 0;
            while (true)
            {
                b = input.read();
                if (b == -1)
                    break;
                out.write((byte) b);
            }
            input.close();
            out.close();
            return outputName;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return outputName;
    }

    private static String getFormat(String filename)
    {
        String format;
        if (!filename.contains("."))
        {
            return null;
        }
        String[] parts = filename.split("\\.");
        format = parts[parts.length - 1];
        for (String availableFormat : attachmentFormats)
        {
            if (format.equals(availableFormat))
            {
                return format;
            }
        }
        return null;
    }

    public static List<MessageBean> filterMessages(List<MessageBean> allMessages)
    {
        List<MessageBean> filteredMessages = new ArrayList<MessageBean>();
        for (MessageBean message : allMessages)
        {
            if (message.getContentParts().length == 0 || isOldLetter(message.getReceivedDate()))
            {
                deleteAttachments(message.getAttachments());
                continue;
            }
            filteredMessages.add(message);
        }
        return filteredMessages;
    }

    private static Message[] filterMessagesByDate(Message[] allMessages)
    {
        List<Message> filteredMessagesList = new ArrayList<Message>();
        if (allMessages == null)
        {
            return null;
        }
        try
        {
            for (Message message : allMessages)
            {
                // also send from filter for now
                if (message.getFrom() != null && message.getFrom().length > 0
                // && message.getFrom()[0].toString().contains("Lesneu")
                )
                    if (!isOldLetter(message.getReceivedDate()))
                    {
                        filteredMessagesList.add(message);
                    }
            }
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }
        Message[] filteredMessages = new Message[filteredMessagesList.size()];
        filteredMessages = filteredMessagesList.toArray(filteredMessages);
        return filteredMessages;
    }

    public static boolean isOldLetter(Date receivedDate)
    {
        int daysInPeriod = daysInPeriod(receivedDate, new Date());
        if (daysInPeriod < 0 || daysInPeriod > AppProperties.getInstance().getIntProperty(Constants.DAYS_PERIOD))
        {
            return true;
        }
        return false;
    }

    private static int daysInPeriod(Date firstDate, Date secondDate)
    {
        if (firstDate == null || secondDate == null)
        {
            return -1;
        }
        return (int) ((secondDate.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    private static void deleteAttachments(List<String> attachments)
    {
        File file = null;
        for (String attachment : attachments)
        {
            file = new File(PATH + attachment);
            if (file.exists())
            {
                file.delete();
            }
        }
    }

    private static String checkForNull(String str)
    {
        if (null == str)
        {
            return "";
        }
        return str;
    }
}
