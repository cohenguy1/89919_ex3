/* Ido Cohen	Guy Cohen	203516992	304840283 */
package InputOutput;

import java.util.*;

public class LidstoneModel 
{
	/*
	 * Returns Lidstone smoothing, by the formula shown in class
	 */
	public static double CalcUnigramPLidstone(double lambda, Map<String, Map<String, Integer>> lidstoneTrainMap, long mapSize, String word)
	{
		long totalWordordOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, word);

		return (totalWordordOccurences + lambda)/(mapSize + lambda*Output.vocabulary_size); 
	}

	
	public static double CalcBigramPLidstone(double lambda, Map<String, Map<String, Integer>> lidstoneTrainMap, String word, String prevWord)
	{
		long prevWordNotLastOccurences;
		if (prevWord == DataClass.BEGIN_ARTICLE)
		{
			prevWordNotLastOccurences = DataClass.wordCountMapInTrainThatAreNotLastWithBeginArticle.get(prevWord) == null ? 0 : DataClass.wordCountMapInTrainThatAreNotLastWithBeginArticle.get(prevWord);
			if (prevWordNotLastOccurences==0)
				System.out.println("stop26");
		}
		else
		{
//			prevWordNotLastOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, prevWord);
			prevWordNotLastOccurences = DataClass.wordCountMapInTrainThatAreNotLastWithBeginArticle.get(prevWord) == null ? 0 : DataClass.wordCountMapInTrainThatAreNotLastWithBeginArticle.get(prevWord);

		}
		
		long wordAfterPrevOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, word, prevWord);

		return (wordAfterPrevOccurences + lambda)/(prevWordNotLastOccurences + lambda * Output.vocabulary_size); 
	}


	public static double getUnigramPLidstone(String word) 
	{
		return (DataClass.trainMapLidstonUnigram.get(word) == null ? DataClass.trainMapLidstonUnigram.get(DataClass.UNSEEN_WORD) : DataClass.trainMapLidstonUnigram.get(word));
	}
}
