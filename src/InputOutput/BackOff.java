package InputOutput;

import java.util.ArrayList;
import java.util.List;
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
//			if (word.equals("his")){
////				System.out.println("his");
//			}


			long wordAfterPrevOccurences = lidstoneTrainMap.get(word) == null ? 0 : (lidstoneTrainMap.get(word).get(prevWord)==null ? 0 : lidstoneTrainMap.get(word).get(prevWord));
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
}
