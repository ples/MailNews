package org.mailnews.helper;

import javax.servlet.AsyncContext;

public class SessionProperties
{
    private String name;
    private int currentPage = 0;
    private int currentMail = 0;
    private int currentColor = 0;
    private AsyncContext itsContext;
    
    public SessionProperties(String aName, AsyncContext aContext)
    {
        setName(aName);
        setItsContext(aContext);
    }
    /**
     * 
     * @return
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getCurrentPage()
    {
        return currentPage;
    }

    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    public int getCurrentMail()
    {
        return currentMail;
    }

    public void setCurrentMail(int currentMail)
    {
        this.currentMail = currentMail;
    }

    public int getCurrentColor()
    {
        return currentColor;
    }

    public void setCurrentColor(int currentColor)
    {
        this.currentColor = currentColor;
    }

    public AsyncContext getItsContext()
    {
        return itsContext;
    }

    public void setItsContext(AsyncContext itsContext)
    {
        this.itsContext = itsContext;
    }
    
    public String toParameterString()
    {
        return "name="+name+"&next="+currentMail+"_"+currentPage+"&color="+currentColor;
    }
}
