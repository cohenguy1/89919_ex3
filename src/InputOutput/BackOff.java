/* Ido Cohen	Guy Cohen	203516992	304840283 */
package InputOutput;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BackOff 
{
	public static String unseenWord = "unseen-word";

	public static Map<String, Double> AlphaValues;

	public static double UNIGRAM_LAMDA = 0.1;

	public static double calcBigramBackOff(double bigramLambda,
			Map<String, Map<String, Integer>> lidstoneTrainMap,
			String word, String prevWord, double prevWordAlphaValue) 
	{
		//check if word appears after prevWord - and use lidstone bigram or unigram
		long wordAfterPrevOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, word, prevWord);
		double pWord;

		if (wordAfterPrevOccurences > 0)
		{
			pWord = LidstoneModel.CalcBigramPLidstone(bigramLambda, lidstoneTrainMap, word, prevWord);
		}
		else 
		{
			pWord = prevWordAlphaValue * LidstoneModel.getUnigramPLidstone(word);
		}

		return pWord;
	}

	public static double GetAlphaValue(String word)
	{
		return AlphaValues.get(word) == null ? 1 : AlphaValues.get(word);
	}

	public static void CalculateAlphaValues(double bigramLambda, Set<String> wordToCalcFor)
	{
		AlphaValues = new TreeMap<String, Double>();

		for (String prevWord : wordToCalcFor)
		{
			AlphaValues.put(prevWord, CalculateAlpha(bigramLambda, prevWord));
		}
	}

	public static double CalculateAlpha(double bigramLambda, String prevWord)
{
		Map<String, Integer> prevWordSequential = DataClass.SequentialMap.get(prevWord);

		if (DataClass.TrainingMap.get(prevWord) == null || prevWordSequential == null)
		{
			return 1;
		}

		double sumPWordPrevWord = 1;
		double sumPWords = 1;

		for(String word : prevWordSequential.keySet())
		{
			long wordAfterPrevOccurences = DataClass.getWordOccurrences(DataClass.TrainingMap, word, prevWord);
			if (wordAfterPrevOccurences > 0)
			{
				sumPWordPrevWord -= LidstoneModel.CalcBigramPLidstone(bigramLambda, DataClass.TrainingMap, word, prevWord);
				sumPWords -= LidstoneModel.getUnigramPLidstone(word);
			}
		}	

		if (sumPWordPrevWord/sumPWords <= 0)
			System.out.println("alpha zero- "+ prevWord +" biLamda-"+bigramLambda);

		return sumPWordPrevWord/sumPWords;
	}

	/*
	 * Checks the sum of p words is 1
	 */
	public static void modelSanityCheck(double bigramLambda, Map<String, Map<String, Integer>> trainMap)
	{		
		long N0 = Output.vocabulary_size - trainMap.keySet().size();
		
		// Prevent inaccuracies by java double calculations
		double epsilon = 0.000000000000002;

		CalculateAlphaValues(bigramLambda, trainMap.keySet());

		for(String word : trainMap.keySet())
		{
			double sum = 0;

			double prevWordAlpha = GetAlphaValue(word);

			// Contribution of all words that don't appear in the training set (unseen events)
			sum += N0 * calcBigramBackOff(bigramLambda, trainMap, unseenWord, word, prevWordAlpha);

			for(String nextWord : trainMap.keySet())
			{
				sum += calcBigramBackOff(bigramLambda, trainMap, nextWord, word, prevWordAlpha);
			}

			if (Math.abs(1 - sum) < epsilon)
			{
				Output.writeConsoleWhenTrue("BackOff is GOOD!");
			}
			else
			{
				System.out.println("BackOff is BAD. Value: " + sum);
			}
		}
	}
}
