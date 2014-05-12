package org.mailnews.classifier;

public class ClassifierSingleton {

	private static ClassifierSingleton itsClassifier;
	
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
}
