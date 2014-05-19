package org.mailnews.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
		System.out.println(maxProbabilityName + ": " + Math.exp(maxProbability) / summProbability);
		if( Math.exp(maxProbability) / summProbability < classMinProbability 
				&& maxProbabilityName.equals(aClassName))
		{
			return reserveClassName;
		}
		return maxProbabilityName;
	}
	
	public static String[] tokenize(String aText)
	{
		List<String> words =new ArrayList<String>(Arrays.asList(aText.toLowerCase().split("([^\\p{L}]+)(\\d*)(_*)")));
		if (words.size()!=0 && "".equals(words.get(0)))
		{
		    words.remove(0);
		}
		return words.toArray(new String[words.size()]);
	}
	
	public double calculateProbability(String aClass, String aText)
	{
		String[] words = tokenize(aText);
		double summaryProbability = 0.0;
		System.out.print(aClass+": ");
		for(String eachWord : words)
		{
		    System.out.print(String.format("%.2f", model.wordLogProbability(aClass, eachWord))+", ");
			summaryProbability += model.wordLogProbability(aClass, eachWord);
		}
		System.out.println(" Class log: " + model.classLogProbability(aClass));
		return summaryProbability + model.classLogProbability(aClass);
	}
}
