package org.mailnews.classifier;

import java.util.ArrayList;
import java.util.List;

public class NaiveBayesLearningAlgorithm {
	private List<String []> examples = new ArrayList<String []>();
	
	public void addExample(String aText, String aClass)
	{
		examples.add(new String[]{ aText, aClass});
	}
	
}
