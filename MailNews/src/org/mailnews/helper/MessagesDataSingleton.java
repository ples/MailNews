package org.mailnews.helper;

public class MessagesDataSingleton {

	private static MessagesDataSingleton instance;
	
	private MessagesDataSingleton()
	{

	}
	
	public static MessagesDataSingleton getInstance()
	{
		if(instance == null)
		{
			instance = new MessagesDataSingleton();
		}
		return instance;
	}
	
}
