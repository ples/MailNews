package org.mailnews.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class AppProperties {

	private static AppProperties instance;
	
	public static AppProperties getInstance()
	{
		if(instance == null)
		{
			instance = new AppProperties();
		}
		return instance;
	}
	
	private Properties props;
	
	private AppProperties()
	{
		props = new Properties();
	}
	
	public boolean loadFromFile(String aPropertyFilePath)
	{
		try
		{
			props.load(new FileInputStream(new File(aPropertyFilePath)));
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
	
	public boolean save(String aPropertyFilePath)
	{
		try
		{
			props.store(new FileOutputStream(new File(aPropertyFilePath)), "");
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
	
	public String getProperty(String propertyName)
	{	
		return props.getProperty(propertyName);
	}
	
	public void setProperty(String propertyName, String value)
	{
		props.put(propertyName, value);
	}
	
	public String[] getArrayProperty(String propertyName){
		String[] values = props.getProperty(propertyName).split(",");
		return values;
	}
	
	public void setArrayProperty(String propertyName, String[] values)
	{
		String margin = "";
		for (int i = 0; i < values.length; i++) {
			margin+=values[i] + 
					(i==values.length-1? "":",");
		}
		props.put(propertyName, margin);
	}
	
	public int[] getIntArrayProperty(String propertyName){
		String[] values = props.getProperty(propertyName).split(",");
		int[] intValues = new int[values.length];
		for (int i = 0; i < values.length; i++) 
		{
			intValues[i] = Integer.parseInt(values[i].trim());
		}
		return intValues;
	}
	
	public void setIntArrayProperty(String propertyName, int[] values)
	{
		String margin = "";
		for (int i = 0; i < values.length; i++) {
			margin+=values[i] + 
					(i==values.length-1? "":",");
		}
		props.put(propertyName, margin);
	}
	
	public int getIntProperty(String propertyName)
	{
		try{
			return Integer.parseInt(props.getProperty(propertyName).trim());
		} catch (Exception e)
		{
			return 0;
		}
	}
	
	public void setIntProperty(String propertyName, int value)
	{
		props.put(propertyName, String.valueOf(value));
	}
	
}
