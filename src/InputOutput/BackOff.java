package InputOutput;

import java.util.Map;

public class BackOff 
{
	public static String unseenWord = "unseen-word";

	static double UNIGRAM_LAMDA = 0.1;

	public static double calcBigramBackOff(double bigramLambda,
			Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingSize,
			String word, String prevWord) 
	{
		//check if word appears after prevWord - and use lidstone bigram or unigram
		long wordAfterPrevOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, word, prevWord);
		double pWord;
		
		if (wordAfterPrevOccurences > 0)
		{
			pWord = LidstoneModel.CalcBigramPLidstone(bigramLambda, lidstoneTrainMap, word, prevWord);
			if (pWord <= 0)
				System.out.println("STOP!!");
		}
		else 
		{
			pWord = LidstoneModel.CalcUnigramPLidstone(UNIGRAM_LAMDA, lidstoneTrainMap, trainingSize, word) 
					* alphaCalculate(prevWord, bigramLambda, lidstoneTrainMap, trainingSize);
			if (pWord <= 0)
				System.out.println("STOP!");
		}
		
		return pWord;
	}

	private static double alphaCalculate(String prevWord,
			double bigramLambda, Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingSize) 
	{
		double sumPWordPrevWord = 1;
		double sumPWords = 1;

//		long tempCount = 0;

//		List<String> tempList = new ArrayList<String>();
//		//		tempList.add("referring");
//		//		tempList.add("domestic");
//		//		tempList.add("tokyo");
//		//		tempList.add("lawson");
//		//		tempList.add("soybean");
//		//		tempList.add("ec");
//		//		tempList.add("residual");
//		//		tempList.add("yeutter");
//		//		tempList.add("begin-article");
//		if (prevWord.equals("said")){
////			System.out.println("hi");
//		}
//		int mycount = 0;
		for (String word : lidstoneTrainMap.keySet())
		{

			long wordAfterPrevOccurences = DataClass.getWordOccurrences(lidstoneTrainMap, word, prevWord);
			if (wordAfterPrevOccurences > 0 ) {
//				mycount++;
//				tempCount+=wordAfterPrevOccurences;

//				System.out.println(mycount+" - " + word + " - " + wordAfterPrevOccurences);

				sumPWordPrevWord -= LidstoneModel.CalcBigramPLidstone(bigramLambda, lidstoneTrainMap, word, prevWord);
				sumPWords -= LidstoneModel.CalcUnigramPLidstone(UNIGRAM_LAMDA, lidstoneTrainMap, trainingSize, word);
			}
		}	

		if (sumPWordPrevWord/sumPWords <= 0)
			System.out.println("alpha zero- "+prevWord+" biLamda-"+bigramLambda);
//		if (tempCount != (lidstoneTrainMap.get(prevWord) == null ? 0 : DataClass.wordsTotalAmountRegu(lidstoneTrainMap,prevWord)))
//			System.out.println("numSTOP!!!!!"+prevWord);

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
		
		for(String word : trainMap.keySet())
		{
			double sum = 0;
			
			// Contribution of all words that don't appear in the training set (unseen events)
			sum += N0 * calcBigramBackOff(bigramLambda, trainMap, trainingSize, unseenWord, word);
			
			for(String nextWord : trainMap.keySet())
			{
				sum += calcBigramBackOff(bigramLambda, trainMap, trainingSize, nextWord, word);
			}
			
			if (Math.abs(1 - sum) < epsilon)
			{
				Output.writeConsoleWhenTrue("BackOff is GOOD!");
			}
			else
			{
				Output.writeConsoleWhenTrue("BackOff is BAD. Value: " + sum);
			}
		}
	}
}
