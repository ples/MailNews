package org.mailnews.helper;

import java.io.File;


public class MessagesDataSingleton {

	private static MessagesDataSingleton instance;
	
	private MessageData itsMessageData;
	
	private static String itsPath;
	
	public MessageData getMessageData() {
		return itsMessageData;
	}

	private MessagesDataSingleton()
	{
		if(itsPath == null)
		{
			itsPath = new File("").getAbsolutePath();
		}
		itsMessageData = new MessageData(itsPath);
	}
	
	public static MessagesDataSingleton getInstance()
	{
		if(instance == null)
		{
			instance = new MessagesDataSingleton();
		}
		return instance;
	}
	
	public static void setPath(String aPath)
	{
		itsPath = aPath;
	}
}
