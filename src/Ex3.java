/* Ido Cohen	Guy Cohen	203516992	304840283 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import InputOutput.BackOff;
import InputOutput.DataClass;
import InputOutput.Output;
import InputOutput.Topics;

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

			// Output 7
			outputClass.writeOutput(DataClass.wordsTotalAmount(validationMap));

			long trainingMapSize = DataClass.wordsTotalAmount(lidstoneTrainMap);
			
			// Output 8
			outputClass.writeOutput(trainingMapSize);

			// Output 9
			outputClass.writeOutput(lidstoneTrainMap.keySet().size()); 

			// Output 10
			outputClass.writeOutput(DataClass.getWordOccurrences(lidstoneTrainMap, inputWord1));

			// Output 11
			outputClass.writeOutput(DataClass.getWordOccurrences(lidstoneTrainMap, inputWord2, inputWord1));

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

			double lambda = 0.001;
			BackOff.modelSanityCheck(lambda, lidstoneTrainMap);

			DataClass testData = new DataClass();
			testData.readInputFile(test_inputFile);

			ArrayList<Event> eventList = new ArrayList<Event>();
			EventComparator eventComparator = new EventComparator();
			
			for (String word : lidstoneTrainMap.keySet())
			{
				long wordAfterInputWordOcc = DataClass.getWordOccurrences(lidstoneTrainMap, word, inputWord1);
				double probability = BackOff.calcBigramBackOff(lambda, lidstoneTrainMap, trainingMapSize, word, inputWord1);
				
				eventList.add(new Event(word, wordAfterInputWordOcc, probability));
			}
			
			eventList.sort(eventComparator);

			// Output 29
			outputClass.writeOutput("");

			for (int i = 0; i < eventList.size(); i++)
			{
				Event event = eventList.get(i);
				outputClass.writeOutputFile("\n"+ i + "\t" + event.word + "\t" + event.occurrencesAfterInputWord + "\t" + event.probability + "\n");
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}			
	}	

	/*
	 * Returns model perplexity 
	 */
	private static double calculatePerplexityByBackOff(double bigramLambda, Map<String, Map<String, Integer>> lidstoneTrainMap, Map<String, Map<String, Integer>> validationMap) 
	{		
		double sumPWords = 0;

		long trainingSize = DataClass.wordsTotalAmount(lidstoneTrainMap);

		for (String word : validationMap.keySet())
		{
			// for each of the prev words of the word in the validation map
			for (String prevWord : validationMap.get(word).keySet())
			{
				long wordAfterPrevOccurences = validationMap.get(word) == null ? 0 : (validationMap.get(word).get(prevWord) == null ? 0 : validationMap.get(word).get(prevWord));
				if(!prevWord.equals(DataClass.FirstArticleWord) && wordAfterPrevOccurences > 0){
					double pWord = BackOff.calcBigramBackOff(bigramLambda,
							lidstoneTrainMap, trainingSize, word,
							prevWord);
					// adds the probability to the sum occurrences time (as the number of sequential occurrences in the validation map)
					sumPWords += wordAfterPrevOccurences * Math.log(pWord)/Math.log(2);
				}
			}
		}

		long validationWordsSize = DataClass.wordsTotalAmount(validationMap);

		double perplexity = Math.pow(2,(-1.0/validationWordsSize) * sumPWords); 
		return perplexity;
	}

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
