package org.mailnews.classifier;

import org.mailnews.properties.Constants;

public class ClassifierSingleton {

	private static ClassifierSingleton itsClassifier;
	
	private String customDictionary = "";
	
	public static ClassifierSingleton getInstance()
	{
		if( itsClassifier == null)
		{
			itsClassifier = new ClassifierSingleton();
		}
		return itsClassifier;
	}
	
	private NaiveBayesLearningAlgorithm itsLearner;
	
	private ClassifierSingleton()
	{
		itsLearner = new NaiveBayesLearningAlgorithm();
		itsLearner.addExample(-1, customDictionary, Constants.SPAM);
	}
	
	public void learn(Integer mailId, String aClass, String aText)
	{
		itsLearner.addExample(mailId, aText, aClass);
	}
	
	public String getClass(String aClass, String aText, double minProbability, String reserveClass)
	{
		return itsLearner.getClassifier().classify(aText, aClass, minProbability, reserveClass);
	}
	
	public void setMailClass(Integer aMailId, String newClass)
	{
	    itsLearner.setClass(aMailId, newClass);
	} 
	
	public void clearDictionary()
	{
	    itsLearner.clearDictionary();
	}
	
	public void refreshClassifier()
	{
	    itsLearner.refresh();
	}

    public String getCustomDictionary()
    {
        return customDictionary;
    }

    public void setCustomDictionary(String customDictionary)
    {
        itsLearner.addExample(-1, customDictionary, Constants.SPAM);
    }
}
