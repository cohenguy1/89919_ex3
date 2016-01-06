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
			Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingSize,
			String word, String prevWord, double prevWordAlphaValue) 
	{
		if (word.equals(DataClass.BEGIN_ARTICLE))
			System.out.println("stop21");
		
		//check if word appears after prevWord - and use lidstone bigram or unigram
		long wordAfterPrevOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, word, prevWord);
		double pWord;

		if (wordAfterPrevOccurences > 0)
		{
			pWord = LidstoneModel.CalcBigramPLidstone(bigramLambda, lidstoneTrainMap, word, prevWord);
			if (pWord <= 0)
				System.out.println("STOP31");
		}
		else 
		{
//			if (prevWord.equals(DataClass.BEGIN_ARTICLE))
//				System.out.println("stop36");
			pWord = prevWordAlphaValue * LidstoneModel.getUnigramPLidstone(word);

			if (pWord <= 0)
				System.out.println("STOP39");
		}

		return pWord;
	}

	public static double GetAlphaValue(String word)
	{
		return AlphaValues.get(word) == null ? 1 : AlphaValues.get(word);
	}

	public static void CalculateAlphaValues(double bigramLambda, Set<String> wordToCalcFor, Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingSizeWithoutBeginArticle)
	{
		AlphaValues = new TreeMap<String, Double>();

		for (String prevWord : wordToCalcFor)
		{
			AlphaValues.put(prevWord, CalculateAlpha(bigramLambda, lidstoneTrainMap, trainingSizeWithoutBeginArticle, prevWord));
		}
		
		AlphaValues.put(DataClass.BEGIN_ARTICLE, CalculateAlpha(bigramLambda, lidstoneTrainMap, trainingSizeWithoutBeginArticle, DataClass.BEGIN_ARTICLE)); 

	}

	public static double CalculateAlpha(double bigramLambda, Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingSize,
			String prevWord)
{
		Map<String, Integer> prevWordSequential = DataClass.mapTotalDocsSequentWords.get(prevWord);

		if ( prevWordSequential == null)
		{
			if (lidstoneTrainMap.get(prevWord) == null)
				System.out.println("stop73");
			return 1;
		}

		double sumPWordPrevWord = 1;
		double sumPWords = 1;
//		int count=0;
		for(String word : prevWordSequential.keySet())
		{
			long wordAfterPrevOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, word, prevWord);
			if (wordAfterPrevOccurences > 0){
				sumPWordPrevWord -= LidstoneModel.CalcBigramPLidstone(bigramLambda, lidstoneTrainMap, word, prevWord);
				sumPWords -= LidstoneModel.getUnigramPLidstone(word);
//				count++;
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
		long trainingSize = DataClass.wordsTotalAmount(trainMap);

		// Prevent inaccuracies by java double calculations
		double epsilon = 0.000000000000002;

		CalculateAlphaValues(bigramLambda, trainMap.keySet(), trainMap, trainingSize);

		for(String word : trainMap.keySet())
		{
			double sum = 0;

			double prevWordAlpha = GetAlphaValue(word);

			// Contribution of all words that don't appear in the training set (unseen events)
			sum += N0 * calcBigramBackOff(bigramLambda, trainMap, trainingSize, unseenWord, word, prevWordAlpha);

			for(String nextWord : trainMap.keySet())
			{
				sum += calcBigramBackOff(bigramLambda, trainMap, trainingSize, nextWord, word, prevWordAlpha);
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

	public static void modelSanityCheck2(Map<String, Map<String, Integer>> trainMap)
	{		
		long N0 = Output.vocabulary_size - trainMap.keySet().size();

		// Prevent inaccuracies by java double calculations
		double epsilon = 0.000000000000002;

		double sum = 0;

		// Contribution of all words that don't appear in the training set (unseen events)
		sum += N0 * LidstoneModel.getUnigramPLidstone("UNSEEN_WORD");

		for(String word : trainMap.keySet())
		{
			sum += LidstoneModel.getUnigramPLidstone(word);
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
