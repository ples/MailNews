package org.mailnews.classifier;

public class NaiveBayesClassifier {
	NaiveBayesModel model;
	
	public String classify(String text)
	{
		return null;
	}
	
	private String[] tokenize(String aText)
	{
		return aText.split("([^\\w])");
	}
	
	public double calculateProbability(String aClass, String aText)
	{
		return 0.0;
	}
}
