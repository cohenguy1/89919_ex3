/* Ido Cohen	Guy Cohen	203516992	304840283 */
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import InputOutput.BackOff;
import InputOutput.DataClass;
import InputOutput.Output;

public class Ex3 
{
	public static long TrainingSize;

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
			long validationSize = DataClass.wordsTotalAmount(validationMap);
			outputClass.writeOutput(validationSize);

			DataClass.CountWordOccurrencesInTrainMap(lidstoneTrainMap);
			
			TrainingSize = DataClass.wordsTotalAmount(lidstoneTrainMap);
			DataClass.trainMapPrevWordCount(lidstoneTrainMap);  
			DataClass.trainMapCalcLidstonUnigram(lidstoneTrainMap, TrainingSize);
			
			// Output 8
			outputClass.writeOutput(TrainingSize);

			// Output 9
			outputClass.writeOutput(lidstoneTrainMap.keySet().size()); 

			// Output 10
			outputClass.writeOutput(DataClass.getWordOccurrences(lidstoneTrainMap, inputWord1));

			// Output 11
			outputClass.writeOutput(DataClass.getWordOccurrences(lidstoneTrainMap, inputWord2, inputWord1));

			// Output 12
			outputClass.writeOutput(calculatePerplexityByBackOff(0.0001, lidstoneTrainMap, validationMap, validationSize));

			// Output 13
			outputClass.writeOutput(calculatePerplexityByBackOff(0.001, lidstoneTrainMap, validationMap, validationSize));

			// Output 14
			outputClass.writeOutput(calculatePerplexityByBackOff(0.1, lidstoneTrainMap, validationMap, validationSize));

			double bestBigramLambda = getBestLambda(lidstoneTrainMap, validationMap, validationSize);

			// Output 15
			outputClass.writeOutput(String.format("%.5f", bestBigramLambda));

			// Output 16
			outputClass.writeOutput(calculatePerplexityByBackOff(bestBigramLambda, lidstoneTrainMap, validationMap, validationSize));

			double lambda = 0.001;
			//BackOff.modelSanityCheck(lambda, lidstoneTrainMap);

			DataClass testData = new DataClass();
			testData.readInputFile(test_inputFile);

			ArrayList<Event> eventList = new ArrayList<Event>();

			double inputWordAlpha = BackOff.CalculateAlpha(lambda, lidstoneTrainMap, TrainingSize, inputWord1);
			
			for (String word : lidstoneTrainMap.keySet())
			{
				long wordAfterInputWordOcc = DataClass.getWordOccurrences(lidstoneTrainMap, word, inputWord1);

				double probability = BackOff.calcBigramBackOff(lambda, lidstoneTrainMap, TrainingSize, word, inputWord1, inputWordAlpha);
				
				eventList.add(new Event(word, wordAfterInputWordOcc, probability));
			}
			
			Collections.sort(eventList, new EventComparator());

			// Output 18
			outputClass.writeOutput("");

			for (int i = 0; i < eventList.size(); i++)
			{
				Event event = eventList.get(i);
				outputClass.writeOutputFile("\n"+ i + "\t" + event.word + "\t" + event.occurrencesAfterInputWord + "\t" + event.probability);
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
	private static double calculatePerplexityByBackOff(double bigramLambda, Map<String, Map<String, Integer>> lidstoneTrainMap, Map<String, Map<String, Integer>> validationMap, long validationSize) 
	{		
		double sumPWords = 0;


	    SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");

		System.out.println("starting alpha calc time: " + ft.format(new Date()));
		BackOff.CalculateAlphaValues(bigramLambda, validationMap.keySet(), lidstoneTrainMap, TrainingSize);
		System.out.println("alpha finished time: " + ft.format(new Date()));
		
		for (String word : validationMap.keySet())
		{
			// for each of the prev words of the word in the validation map
			for (String prevWord : validationMap.get(word).keySet())
			{
				long wordAfterPrevOccurences = DataClass.getWordOccurrences(validationMap, word, prevWord);
				
				//if(wordAfterPrevOccurences > 0)	
				//{
					double pWord = BackOff.calcBigramBackOff(bigramLambda,	lidstoneTrainMap, TrainingSize, word,
							prevWord, BackOff.GetAlphaValue(prevWord));
					
					// adds the probability to the sum occurrences time (as the number of sequential occurrences in the validation map)
					sumPWords += wordAfterPrevOccurences * Math.log(pWord)/Math.log(2);
				//}
			}
		}

		System.out.println("finished loop " + ft.format(new Date()));
		
		double perplexity = Math.pow(2,(-1.0/validationSize) * sumPWords); 
		return perplexity;
	}

	private static double getBestLambda(Map<String, Map<String, Integer>> lidstoneTrainMap, Map<String, Map<String, Integer>> validationMap, long validationSize)
	{
		double bestLambdaIndex = 0.0001;
		double bestPerplexityValue = calculatePerplexityByBackOff(bestLambdaIndex, lidstoneTrainMap, validationMap, validationSize);

		double perplexity;

		// iterate over the lambdas from 0.0001 to 0.02 (1 to 200 divided by 10,000, for accuracies) 
		double DIVIDE_LAMDA = 10000.0;
		
	    SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");

		for (int lambdaIndex = 2; lambdaIndex <= 200; lambdaIndex++)
		{
			Date dNow = new Date();
			System.out.println("lambda = " + lambdaIndex/DIVIDE_LAMDA + ", time: " + ft.format(dNow));
			
			// calculate the perplexity by this lambda
			perplexity = calculatePerplexityByBackOff(lambdaIndex/DIVIDE_LAMDA, lidstoneTrainMap, validationMap, validationSize);

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
