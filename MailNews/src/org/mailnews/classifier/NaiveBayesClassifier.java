package org.mailnews.classifier;

import java.util.ArrayList;
import java.util.List;

public class NaiveBayesClassifier {
	NaiveBayesModel model;

	public NaiveBayesClassifier(NaiveBayesModel model) 
	{
		super();
		this.model = model;
	}

	public String classify(String text, String aClassName, double classMinProbability, String reserveClassName)
	{
		double maxProbability = - Double.MAX_VALUE;
		double summProbability = 0.0;
		List<Double> probs = new ArrayList<Double>();
		String maxProbabilityName = "None";
		String [] classes = model.classes().toArray(new String[]{});
		for(String eachClass: classes)
		{
			double probability = calculateProbability(eachClass, text);
			summProbability += Math.exp(probability);
			probs.add(Double.valueOf(probability));
			if( probability > maxProbability)
			{
				maxProbability = probability;
				maxProbabilityName = eachClass;
			}
		}
		if( Math.exp(maxProbability) / summProbability < classMinProbability 
				&& maxProbabilityName.equals(aClassName))
		{
			return reserveClassName;
		}
		return maxProbabilityName;
	}
	
	public static String[] tokenize(String aText)
	{
		String [] words = aText.toLowerCase().split("([\\p{Punct}\\s]+)");
		return words;
	}
	
	public double calculateProbability(String aClass, String aText)
	{
		String[] words = tokenize(aText);
		double summaryProbability = 0.0;
		for(String eachWord : words)
		{
			summaryProbability += model.wordLogProbability(aClass, eachWord);
		}
		return summaryProbability + model.classLogProbability(aClass);
	}
}
