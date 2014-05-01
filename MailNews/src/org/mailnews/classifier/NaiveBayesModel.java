package org.mailnews.classifier;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NaiveBayesModel {
	private Map<String, Integer> length;
	private Map<String, Integer> docCount;
	private Map<String, Map<String, Integer>> wordCount;
	private Integer dictionarySize;
	
	public NaiveBayesModel(Map<String, Integer> length,
			Map<String, Integer> docCount,
			Map<String, Map<String, Integer>> wordCount, Integer dictionarySize) {
		super();
		this.length = length;
		this.docCount = docCount;
		this.wordCount = wordCount;
		this.dictionarySize = dictionarySize;
	}

	public double wordLogProbability(String c, String w)
	{
		Integer myWordCount = wordCount.get(c).get(w);
		myWordCount = myWordCount == null ? Integer.valueOf(0) : myWordCount;
		return Math.log((myWordCount + 1.0) / 
				((double)length.get(c) + dictionarySize));
	}
	
	public double classLogProbability(String c)
	{
		double ret = Math.log((double)docCount.get(c) / getSumFromMap(docCount));
		return ret;
	}	
	
	public Set<String> classes()
	{
		return docCount.keySet();
	}
	
	
	
	
	
	
	private double getSumFromMap(Map<String, Integer> aMap)
	{
		double ret = 0.0;
		for(Iterator<Integer> i = aMap.values().iterator(); i.hasNext();)
		{
			ret += i.next();
		}
		return ret;
	}
}
