package InputOutput;

import java.util.Map;

public class BackOff {

	static double UNIGRAM_LAMDA = 0.1;

	public static double calcBigramBackOff(double bigramLambda,
			Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingSize,
			String word, String prevWord) {

		//check if word appears after prevWord - and use lidstone bigram or unigram
		long wordAfterPrevOccurences = lidstoneTrainMap.get(word) == null ? 0 : (lidstoneTrainMap.get(word).get(prevWord)==null ? 0 : lidstoneTrainMap.get(word).get(prevWord));
		double pWord;
		if (wordAfterPrevOccurences > 0){
			pWord = LidstoneModel.CalcBigramPLidstone(bigramLambda, lidstoneTrainMap, word, prevWord);
			if (pWord <= 0)
				System.out.println("STOP!!");
		}
		else {
			pWord = LidstoneModel.CalcUnigramPLidstone(UNIGRAM_LAMDA, lidstoneTrainMap, trainingSize, word) 
					* alphaCalculate(prevWord,bigramLambda,lidstoneTrainMap, trainingSize);
			if (pWord <= 0)
				System.out.println("STOP!");
		}
		return pWord;
	}

	private static double alphaCalculate(String prevWord,
			double bigramLambda, Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingSize) {

		double sumPWordPrevWord = 1;
		double sumPWords = 1;
		
//		long tempCount =0;

		for (String word : lidstoneTrainMap.keySet())
		{
			long wordAfterPrevOccurences = lidstoneTrainMap.get(word) == null ? 0 : (lidstoneTrainMap.get(word).get(prevWord)==null ? 0 : lidstoneTrainMap.get(word).get(prevWord));
//			tempCount+=wordAfterPrevOccurences;
			if (wordAfterPrevOccurences > 0) {	
				sumPWordPrevWord -= LidstoneModel.CalcBigramPLidstone(bigramLambda, lidstoneTrainMap, word, prevWord);
				sumPWords -= LidstoneModel.CalcUnigramPLidstone(UNIGRAM_LAMDA, lidstoneTrainMap, trainingSize, word);
			}
		}	
		
		if (sumPWordPrevWord/sumPWords <= 0)
			System.out.println("STOP!!!!!");
		return sumPWordPrevWord/sumPWords;
	}
}
