/* Ido Cohen	Guy Cohen	203516992	304840283 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import InputOutput.BackOff;
import InputOutput.DataClass;
import InputOutput.Output;

public class Ex3 
{
	public static long TrainingSizeWithoutBeginArticle;

	public static void main(String[] args) 
	{
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
			outputClass.writeOutput(devData.getTotalWordsInDocsWithoutBeginArticle());

			Map<String, Map<String, Integer>> validationMap  = new TreeMap<String, Map<String, Integer>>();
			devData.splitXPrecentOfDocsWords(0.9, validationMap);

			// Output 7
			long validationSizeWithoutBeginArticle = DataClass.wordsTotalAmountWithoutBeginArticle(validationMap);
			outputClass.writeOutput(validationSizeWithoutBeginArticle);

			DataClass.keepCountWordOccurrencesMapInTrainThatHavePrev();

			TrainingSizeWithoutBeginArticle = DataClass.wordsTotalAmountWithoutBeginArticle(DataClass.TrainingMap);
			DataClass.keepWordCountMaptInTrainTharAreNotLast();  
			DataClass.trainMapCalcLidstonUnigram(TrainingSizeWithoutBeginArticle);

			// Output 8
			outputClass.writeOutput(TrainingSizeWithoutBeginArticle);

			// Output 9
			outputClass.writeOutput(DataClass.TrainingMap.keySet().size() - 1); //without begin-article 

			// Output 10
			outputClass.writeOutput(DataClass.getWordOccurrences(DataClass.TrainingMap, inputWord1));

			// Output 11
			outputClass.writeOutput(DataClass.getWordOccurrences(DataClass.TrainingMap, inputWord2, inputWord1));

			// Output 12
			outputClass.writeOutput(calculatePerplexityByBackOff(0.0001, validationMap, validationSizeWithoutBeginArticle));

			// Output 13
			outputClass.writeOutput(calculatePerplexityByBackOff(0.001, validationMap, validationSizeWithoutBeginArticle));

			// Output 14
			outputClass.writeOutput(calculatePerplexityByBackOff(0.1, validationMap, validationSizeWithoutBeginArticle));

			double bestBigramLambda = getBestLambda(validationMap, validationSizeWithoutBeginArticle);

			// Output 15
			outputClass.writeOutput(String.format("%.4f", bestBigramLambda));

			// Output 16
			outputClass.writeOutput(calculatePerplexityByBackOff(bestBigramLambda, validationMap, validationSizeWithoutBeginArticle));

			double lambda = 0.001;
			//BackOff.modelSanityCheck(lambda, lidstoneTrainMap);

			long testSizeWithoutBeginArticle = devData.readTestFile(test_inputFile);

			// Output 17
			outputClass.writeOutput(calculatePerplexityByBackOff(bestBigramLambda, DataClass.TestMap, testSizeWithoutBeginArticle));

			ArrayList<Event> eventList = new ArrayList<Event>();

			double inputWordAlpha = BackOff.CalculateAlpha(lambda, inputWord1);

			for (String word : DataClass.TrainingMap.keySet())
			{
				if (word == DataClass.BEGIN_ARTICLE)
				{
					continue;
				}

				long wordAfterInputWordOcc = DataClass.getWordOccurrences(DataClass.TrainingMap, word, inputWord1);

				double probability = BackOff.calcBigramBackOff(lambda, DataClass.TrainingMap, word, inputWord1, inputWordAlpha);

				eventList.add(new Event(word, wordAfterInputWordOcc, probability));
			}

			Collections.sort(eventList, new EventComparator());

			// Output 18
			outputClass.writeOutput("");

			for (int i = 0; i < eventList.size(); i++)
			{
				Event event = eventList.get(i);
				outputClass.writeOutputFile("\n" + (i + 1) + "\t" + event.word + "\t" + event.occurrencesAfterInputWord + "\t" + event.probability);
			}

			long unseenEventCount = Output.vocabulary_size - eventList.size();
			String unseenEvent = "UNSEEN_EVENT";
			double unseenEventProbability = BackOff.calcBigramBackOff(lambda, DataClass.TrainingMap, unseenEvent, inputWord1, inputWordAlpha);
			outputClass.writeOutputFile("\n" + unseenEventCount + "\t" + unseenEvent + "\t" + 0 + "\t" + unseenEventProbability);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}			
	}	

	/*
	 * Returns model perplexity 
	 */
	private static double calculatePerplexityByBackOff(double bigramLambda, Map<String, Map<String, Integer>> validationMap, long validationSizeWithoutBeginArticle) 
	{		
		double sumPWords = 0;

		BackOff.CalculateAlphaValues(bigramLambda, validationMap.keySet());

		for (String word : validationMap.keySet())
		{			
			if (word == DataClass.BEGIN_ARTICLE)
			{
				continue;
			}

			// for each of the prev words of the word in the validation map
			for (String prevWord : validationMap.get(word).keySet())
			{
				long wordAfterPrevOccurences = DataClass.getWordOccurrences(validationMap, word, prevWord);

				double pWord = BackOff.calcBigramBackOff(bigramLambda, DataClass.TrainingMap, word,
						prevWord, BackOff.GetAlphaValue(prevWord));

				// adds the probability to the sum occurrences time (as the number of sequential occurrences in the validation map)
				sumPWords += wordAfterPrevOccurences * Math.log(pWord)/Math.log(2);
			}
		}

		double perplexity = Math.pow(2,(-1.0/validationSizeWithoutBeginArticle) * sumPWords); 
		return perplexity;
	}

	private static double getBestLambda(Map<String, Map<String, Integer>> validationMap, long validationSize)
	{
		double bestLambdaIndex = 0.0001;
		double bestPerplexityValue = calculatePerplexityByBackOff(bestLambdaIndex, validationMap, validationSize);

		double perplexity;

		// iterate over the lambdas from 0.0001 to 0.02 (1 to 200 divided by 10,000, for accuracies) 
		double DIVIDE_LAMDA = 10000.0;

		for (int lambdaIndex = 2; lambdaIndex <= 200; lambdaIndex++)
		{
			// calculate the perplexity by this lambda
			perplexity = calculatePerplexityByBackOff(lambdaIndex/DIVIDE_LAMDA, validationMap, validationSize);

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
