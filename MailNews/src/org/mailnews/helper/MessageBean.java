package org.mailnews.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String subject;
	private String from;
	private String to;
	private Date receivedDate;
	private String content;
	private String[] contentParts;
	private boolean isNew;
	private int msgId;
	private List<String> attachments;
	private String encoding;
	private boolean isSpam = false;
	private boolean ignoreFilter = false;

	public MessageBean(int msgId, String subject, String from, String to,
			Date receivedDate, String content, boolean isNew,
			ArrayList<String> attachments, String enc) {
		this.subject = subject;
		this.from = from;
		this.to = to;
		this.receivedDate = receivedDate;
		this.content = content;
		this.isNew = isNew;
		this.msgId = msgId;
		this.attachments = attachments;
		this.setEncoding(enc);
		if (attachments == null) {
			attachments = new ArrayList<String>();
		}
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String[] getContentParts() {
		return contentParts;
	}

	public void setContentParts(String[] contentParts) {
		this.contentParts = contentParts;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean aNew) {
		isNew = aNew;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public List<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<String> attachments) {
	    if(attachments!=null)
	        this.attachments = new ArrayList<String>(attachments);
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

    public boolean isSpam()
    {
        return isSpam;
    }

    public void setSpam(boolean isSpam)
    {
        this.isSpam = isSpam;
    }

    public boolean isIgnoreFilter()
    {
        return ignoreFilter;
    }

    public void setIgnoreFilter(boolean ignoreFilter)
    {
        this.ignoreFilter = ignoreFilter;
    }

}
