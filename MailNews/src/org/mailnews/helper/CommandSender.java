package org.mailnews.helper;

public interface CommandSender extends Runnable
{
    public void setClient(String aClient);
    
    public void setCommand(String aCommand);
}
