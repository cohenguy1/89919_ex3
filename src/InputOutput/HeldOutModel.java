/* Ido Cohen	Guy Cohen	203516992	304840283 */
package InputOutput;

import java.util.Collections;
import java.util.Map;

public class HeldOutModel 
{
	static String unseenWord = "unseen-word";
	
	public static double CalcPHeldOut(Map<String, Integer> trainMap, Map<String, Integer> heldOutMap, String inputWord)
	{
		int occurrences = trainMap.get(inputWord) == null ? 0 : trainMap.get(inputWord);
		
		return CalcPHeldOut(trainMap, heldOutMap, occurrences); 
	}
	
	/*
	 * Returns P held out by the formula shown in calss
	 */
	public static double CalcPHeldOut(Map<String, Integer> trainMap, Map<String, Integer> heldOutMap, int occurrences)
	{
		long Nr = calcNr(trainMap, occurrences);
		
		// in case no word appears occurrences times
		if (Nr == 0)
		{
			return 0;
		}
		
		long Tr = calcTr(trainMap, heldOutMap, occurrences);
		long eventsInHeldOut = DataClass.wordsTotalAmount(heldOutMap);

		return (double)(Tr)/(Nr*eventsInHeldOut); 
	}
	
	/*
	 * Gets how many words appear r times in the training map
	 */
	public static long calcNr(Map<String, Integer> trainMap, int occurrences)
	{
		if (occurrences == 0)
		{
			// All words that don't appear in the training set
			return Output.vocabulary_size - trainMap.keySet().size();
		}
		
		long count = 0;
		
		for(int numOfOccurences : trainMap.values())
		{
			if (numOfOccurences == occurrences)
			{
				count++;
			}
		}
		
		return count;
	}
	
	public static long calcTr(Map<String, Integer> trainMap, Map<String, Integer> heldOutMap, int occurrences)
	{
		long Tr = 0;

		if (occurrences != 0)
		{
			// Gets the number of occurrences in held out map of the words that appear 
			// Occurrences time in the training map
			for (String word : trainMap.keySet())
			{
				if(trainMap.get(word).equals(occurrences))
				{
					Tr += (heldOutMap.get(word) == null? 0 : heldOutMap.get(word)); 
				}
			}
		}
		else
		{
			// In case occurrences is 0, count the number of words that appear
			// in the held out set but don't appear in the training set
			for (String word : heldOutMap.keySet())
			{
				// Only count occurrences of words that don't appear in training
				if(trainMap.get(word) == null)
				{
					Tr += heldOutMap.get(word); 
				}
			}
		}

		return Tr;
	}

	/*
	 * Checks the sum of p words is 1
	 */
	public static void modelSanityCheck(Map<String, Integer> trainMap, Map<String, Integer> heldOutMap)
	{
		int maxOccurrences = Collections.max(trainMap.values());
		
		double p = 0;
		double sum = 0;
		
		// sum from r=0 to r=MaxOccurrences
		for (int occurrences = 0; occurrences <= maxOccurrences; occurrences++)
		{
			p = CalcPHeldOut(trainMap, heldOutMap, occurrences);
			sum += p * calcNr(trainMap, occurrences);
		}
		
		// Prevent inaccuracies by java double calculations
		double epsilon = 0.000000000000001;
		if (Math.abs(1 - sum) < epsilon)
		{
			Output.writeConsoleWhenTrue("HeldOut is GOOD!");
		}
		else
		{
			Output.writeConsoleWhenTrue("HeldOut is BAD. Value: " + sum);
		}
		
	}
}
