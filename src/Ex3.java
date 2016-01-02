/* Ido Cohen	Guy Cohen	203516992	304840283 */
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import InputOutput.BackOff;
import InputOutput.DataClass;
import InputOutput.Output;

public class Ex3 {

	static String unseenWord = "unseen-word";

	public static void main(String[] args) {

		String devl_inputFile = args[0];
		String test_inputFile = args[1];
		String inputWord1 = args[2];
		String inputWord2 = args[3];
		String outputFile = args[4];

		Output outputClass = new Output(outputFile);

		//Output init
		outputClass.writeNames();
		outputClass.writeOutput(devl_inputFile);
		outputClass.writeOutput(test_inputFile);
		outputClass.writeOutput(inputWord1 + " " + inputWord2);

		outputClass.writeOutput(outputFile);
		outputClass.writeOutput(Output.vocabulary_size);

		try 
		{
			DataClass devData = new DataClass();
			devData.readInputFile(devl_inputFile);

			// Output 6
			outputClass.writeOutput(devData.getTotalWordsInDocs());

			Map<String, Map<String, Integer>> lidstoneTrainMap = new TreeMap<String, Map<String, Integer>>();
			Map<String, Map<String, Integer>> validationMap  = new TreeMap<String, Map<String, Integer>>();
			devData.splitXPrecentOfDocsWords(0.9, lidstoneTrainMap, validationMap);
			DataClass.trainMapPrevWordCount(lidstoneTrainMap);
			// Output 7
			outputClass.writeOutput(DataClass.wordsTotalAmount(validationMap));

			// Output 8
			outputClass.writeOutput(DataClass.wordsTotalAmount(lidstoneTrainMap));

			// Output 9
			outputClass.writeOutput(lidstoneTrainMap.keySet().size()); 

			// Output 10
			outputClass.writeOutput(getNumberOfOccurences(lidstoneTrainMap, inputWord1));

			// Output 11
			outputClass.writeOutput(getNumberOfOccurences(lidstoneTrainMap, inputWord2, inputWord1));

			// Output 12
			outputClass.writeOutput(calculatePerplexityByBackOff(0.0001, lidstoneTrainMap, validationMap));

			// Output 13
			outputClass.writeOutput(calculatePerplexityByBackOff(0.001, lidstoneTrainMap, validationMap));

			// Output 14
			outputClass.writeOutput(calculatePerplexityByBackOff(0.1, lidstoneTrainMap, validationMap));

			double bestBigramLambda = getBestLambda(lidstoneTrainMap, validationMap);

			// Output 15
			outputClass.writeOutput(bestBigramLambda);

			// Output 16
			outputClass.writeOutput(calculatePerplexityByBackOff(bestBigramLambda, lidstoneTrainMap, validationMap));














			/*			
			outputClass.writeOutput(calcPMle(lidstoneTrainMap, inputWord));

			// Output 13
			outputClass.writeOutput(calcPMle(lidstoneTrainMap, unseenWord));

			double lambda = 0.1;			

			// Output 14
			outputClass.writeOutput(LidstoneModel.CalcPLidstone(lambda, lidstoneTrainMap, inputWord));

			// Output 15
			outputClass.writeOutput(LidstoneModel.CalcPLidstone(lambda, lidstoneTrainMap, unseenWord));

			// Output 16
			outputClass.writeOutput(calculatePerplexityByLidstone(0.01, lidstoneTrainMap, validationMap));

			// Output 17
			outputClass.writeOutput(calculatePerplexityByLidstone(0.10, lidstoneTrainMap, validationMap));

			// Output 18
			outputClass.writeOutput(calculatePerplexityByLidstone(1.00, lidstoneTrainMap, validationMap));

			double bestLambda = getBestLambda(lidstoneTrainMap, validationMap);

			// Output 19
			outputClass.writeOutput(bestLambda);

			// Output 20
			outputClass.writeOutput(calculatePerplexityByLidstone(bestLambda, lidstoneTrainMap, validationMap));

			Map<String, Integer> heldOutTrainMap = new TreeMap<String, Integer>();
			Map<String, Integer> heldOutMap  = new TreeMap<String, Integer>();
			devData.splitXPrecentOfDocsWords(0.5, heldOutTrainMap, heldOutMap);

			// Output 21
			outputClass.writeOutput(DataClass.wordsTotalAmount(heldOutTrainMap));

			// Output 22
			outputClass.writeOutput(DataClass.wordsTotalAmount(heldOutMap));

			lambda = 0.1;
			LidstoneModel.modelSanityCheck(lambda, lidstoneTrainMap);
			HeldOutModel.modelSanityCheck(heldOutTrainMap, heldOutMap);

			DataClass testData = new DataClass();
			testData.readInputFile(test_inputFile);

			// Output 25
			outputClass.writeOutput(testData.getTotalWordsInDocs());

			double lidstonePerplexity = calculatePerplexityByLidstone(bestLambda, lidstoneTrainMap, testData.getMapTotalDocsWords());

			// Output 26
			outputClass.writeOutput(lidstonePerplexity);

			double heldOutPerplexity = calculatePerplexityByHeldOut(heldOutTrainMap, heldOutMap, testData.getMapTotalDocsWords());

			// output 27
			outputClass.writeOutput(heldOutPerplexity);

			// output 28 
			outputClass.writeOutput(lidstonePerplexity <= heldOutPerplexity ? "L" : "H"); 

			// Output 29
			outputClass.writeOutput("");
			for (int i = 0; i < 10; i++)
			{
				double fr = DataClass.wordsTotalAmount(lidstoneTrainMap) * LidstoneModel.CalcPLidstone(bestLambda, lidstoneTrainMap, i); 
				double fH = DataClass.wordsTotalAmount(heldOutTrainMap) * HeldOutModel.CalcPHeldOut(heldOutTrainMap, heldOutMap, i);
				long Nr = HeldOutModel.calcNr(heldOutTrainMap, i);
				long tr = HeldOutModel.calcTr(heldOutTrainMap, heldOutMap, i);
				outputClass.writeOutputFile("\n"+ i + "\t" + String.format("%.5f", fr) + "\t" + String.format("%.5f", fH) + "\t" + Nr + "\t" + tr + "\t");
			}*/
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}			
	}	

	private static int getNumberOfOccurences(Map<String, Map<String, Integer>> map, String word)
	{
		if (map.get(word) == null)
		{
			return 0;
		}

		int count = 0;
		for (int wordCount : map.get(word).values())
		{
			count += wordCount;
		}

		return count; 
	}

	private static int getNumberOfOccurences(Map<String, Map<String, Integer>> map, String word1, String word2)
	{
		if (map.get(word1) == null)
		{
			return 0;
		}

		return map.get(word1).get(word2) == null ? 0 : map.get(word1).get(word2); 
	}


	//Returns model perplexity

	private static double calculatePerplexityByBackOff(double bigramLambda, Map<String, Map<String, Integer>> lidstoneTrainMap, Map<String, Map<String, Integer>> validationMap) 
	{		
		double sumPWords = 0;

		long trainingSize = DataClass.wordsTotalAmount(lidstoneTrainMap);

		for (String word : validationMap.keySet())
		{
			for (String prevWord : validationMap.get(word).keySet()){

				long wordAfterPrevOccurences = validationMap.get(word) == null ? 0 : (validationMap.get(word).get(prevWord)==null ? 0 : validationMap.get(word).get(prevWord));				
				if(!prevWord.equals(DataClass.First_Article_Word) && wordAfterPrevOccurences > 0){
					double pWord = BackOff.calcBigramBackOff(bigramLambda,
							lidstoneTrainMap, trainingSize, word,
							prevWord);
					// adds the probability to the sum occurrences time (as the number of sequental occurrences in the validation map)
					sumPWords += wordAfterPrevOccurences * Math.log(pWord)/Math.log(2);			}
			}
		}

		long validationWordsSize = DataClass.wordsTotalAmount(validationMap);

		double perplexity = Math.pow(2,(-1.0/validationWordsSize) * sumPWords); 
		return perplexity;
	}




	//	private static double calculatePerplexityByHeldOut(Map<String, Integer> heldOutTrainMap, Map<String, Integer> heldOutMap, Map<String, Integer> validationMap)
	//	{
	//		double sumPWords = 0;
	//
	//		for (String word : validationMap.keySet())
	//		{
	//			double pWord = HeldOutModel.CalcPHeldOut(heldOutTrainMap, heldOutMap, word);
	//
	//			// adds the probability to the sum occurrences time (as the number of occurrences in the validation map)
	//			sumPWords += validationMap.get(word) * Math.log(pWord)/Math.log(2);
	//		}
	//
	//		long validationWordsSize = DataClass.wordsTotalAmount(validationMap);
	//
	//		double perplexity = Math.pow(2,(-1.0/validationWordsSize) * sumPWords); 
	//		return perplexity;
	//	}

	private static double getBestLambda(Map<String, Map<String, Integer>> lidstoneTrainMap, Map<String, Map<String, Integer>> validationMap)
	{
		double bestLambdaIndex = 0.0001;
		double bestPerplexityValue = calculatePerplexityByBackOff(bestLambdaIndex, lidstoneTrainMap, validationMap);

		double perplexity;

		// iterate over the lambdas from 0.0001 to 0.02 (1 to 200 divided by 10,000, for accuracies) 
		double DIVIDE_LAMDA = 10000.0;
		for (int lambdaIndex = 2; lambdaIndex <= 200; lambdaIndex++)
		{
			// calculate the perplexity by this lambda
			perplexity = calculatePerplexityByBackOff(lambdaIndex/DIVIDE_LAMDA, lidstoneTrainMap, validationMap);

			// compare to the best lambda perplexity value thus far
			if (perplexity < bestPerplexityValue)
			{
				bestLambdaIndex = lambdaIndex;
				bestPerplexityValue = perplexity;
			}
		}

		// return the best lambda
		return bestLambdaIndex/DIVIDE_LAMDA;
	}

}
