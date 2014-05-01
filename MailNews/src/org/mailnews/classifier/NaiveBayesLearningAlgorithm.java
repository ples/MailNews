package org.mailnews.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NaiveBayesLearningAlgorithm {
	private List<String []> examples = new ArrayList<String []>();
	
	private NaiveBayesClassifier classifier;
	private NaiveBayesModel model;
	
	
	public NaiveBayesModel getModel() {
		return model;
	}


	public NaiveBayesClassifier getClassifier() {
		model = new NaiveBayesModel(getClassLengths(), 
				getDocCount(), getWordCount(), Integer.valueOf(getDictionary().size()));
		classifier = new NaiveBayesClassifier(model);
		return classifier;
	}

	public Map<String, Integer> getClassLengths()
	{
		Map<String, Integer> classLengths = new HashMap<String, Integer>();
		Map<String, Map<String, Integer>> wordCount = getWordCount();
		for(Iterator<String> iterator = wordCount.keySet().iterator(); iterator.hasNext();)
		{
			String className = iterator.next();
			Map<String, Integer> myWordCount = wordCount.get(className);
			int count = 0;
			for(Iterator<Integer> eachWordCount = myWordCount.values().iterator();eachWordCount.hasNext();)
			{
				count += eachWordCount.next();
			}
			classLengths.put(className, new Integer(count));
		}
		return classLengths;
	}
	
	public void addExample(String aText, String aClass)
	{
		examples.add(new String[]{ aText, aClass});
	}
	
	public Set<String> getDictionary()
	{
		Set<String> dictionary = new HashSet<>();
		for(String[] eachText: examples)
		{		
			String[] words = NaiveBayesClassifier.tokenize(eachText[0]);
			for(String word: words)
			{
				dictionary.add(word);
			}
		}
		return dictionary;
	}
	
	public Map<String, Map<String, Integer>> getWordCount()
	{
		Map<String, Map<String, Integer>> wordsCount = new HashMap<String, Map<String, Integer>>();
		for(String[] eachDoc : examples)
		{
			Map<String,Integer> currentClass = wordsCount.get(eachDoc[1]);
			if(currentClass == null)
			{
				currentClass = new HashMap<String, Integer>();
				wordsCount.put(eachDoc[1], currentClass);
			}
			String [] words = NaiveBayesClassifier.tokenize(eachDoc[0]);
			for(String eachWord : words)
			{
				Integer wordCount = currentClass.get(eachWord);
				if( wordCount == null)
				{
					wordCount = new Integer(1);
				} 
				else
				{
					wordCount ++;
				}
				currentClass.put(eachWord, wordCount);
			}
		}
		return wordsCount;
	}
	
	public Map<String, Integer> getDocCount()
	{
		Map<String, Integer> docCount = new HashMap<String, Integer>();
		for(String[] eachDoc : examples)
		{
			if(docCount.get(eachDoc[1]) == null)
			{
				docCount.put(eachDoc[1], new Integer(1));
			}
			else
			{
				docCount.put(eachDoc[1], docCount.get(eachDoc[1])+1);
			}
		}
		return docCount;
	}
	
}
