/* Ido Cohen	Guy Cohen	203516992	304840283 */
package InputOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class DataClass {

	public static String UNSEEN_WORD = "unseen-word";
	public static String FirstArticleWord = "begin-article";
	private boolean skipLine = true;

	private List<Set<Topics>> docsTopicList;

	private List<Map<String,Map<String, Integer>>> docsMapList;
	private List<Map<String,Map<String, Integer>>> docsSequentialList;

	private List<String> docsStringList;

	private Map<String, Map<String, Integer>> mapTotalDocsWords;
	public static Map<String, Map<String, Integer>> mapTotalSequentialDocsWords;
	private long totalWordsInDocs;
	
	public static Map<String, Long> trainMapWordCount = new HashMap<String, Long>();
	
	public static Map<String, Integer> trainMapNotLastWordCount = new HashMap<String, Integer>();
	public static Map<String, Double> trainMapLidstonUnigram = new HashMap<String, Double>();


	public DataClass(){
		this.docsTopicList = new ArrayList<Set<Topics>>();
		this.docsMapList = new ArrayList<Map<String,Map<String, Integer>>>();
		this.docsSequentialList = new ArrayList<Map<String,Map<String, Integer>>>();
		this.docsStringList = new ArrayList<String>();
	}

	/*
	 * Parse the input file
	 */
	public void readInputFile(String inputFile) throws IOException{

		Output.writeConsoleWhenTrue(Output.folderPath+inputFile);

		FileReader fileReader = new FileReader(Output.folderPath+inputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String docTopicLine;

		while ((docTopicLine = bufferedReader.readLine()) != null) {

			docsTopicList.add(setTopicFromLine(docTopicLine));

			skipEmptyLine(bufferedReader);

			String docTextLine = bufferedReader.readLine();
			mapWordCount(docTextLine);
			
			docsStringList.add(docTextLine);

			skipEmptyLine(bufferedReader);

		}
		fileReader.close();

		mapTotalDocsWords = mapTotalDocsWordCount(this.docsMapList);
		mapTotalSequentialDocsWords = mapTotalDocsWordCount(this.docsSequentialList);
		totalWordsInDocs = wordsTotalAmount(mapTotalDocsWords);
	}

	private void skipEmptyLine(BufferedReader bufferedReader) {
		try {
			if(skipLine)
				bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private Set<Topics> setTopicFromLine(String docTopicLine) {
		// TODO Change in EX4
		Output.writeConsoleWhenTrue(docTopicLine);
		return null;

	}

	/*
	 * Adds each word of the line read to the word mapping 
	 */
	private void mapWordCount(String inputLine) 
	{
		Map<String, Map<String, Integer>> wordsMap = new TreeMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> sequentialMap = new TreeMap<String, Map<String, Integer>>();

		String[] words = inputLine.split(" ");
		String prevWord = FirstArticleWord;

		for(String word : words)
		{
			AddWordToMap(wordsMap, word, prevWord);
			AddToSequentialMap(sequentialMap, word, prevWord);

			prevWord = word;
		}

		docsMapList.add(wordsMap);
		docsSequentialList.add(sequentialMap);

	}

	private void AddToSequentialMap(Map<String, Map<String, Integer>> prevWordsMap,
			String word, String prevWord) {
		AddWordToMap(prevWordsMap, prevWord, word);	
	}

	private void AddWordToMap(Map<String, Map<String, Integer>> wordsMap, String word, String prevWord)
	{
		word = word.toLowerCase();
		prevWord = prevWord.toLowerCase();

		Map<String, Integer> wordMap = wordsMap.get(word);

		if (wordMap == null)
		{
			wordsMap.put(word, new TreeMap<String, Integer>());
		}

		wordMap = wordsMap.get(word);

		wordMap.put(prevWord, wordMap.get(prevWord) == null ? 1 : wordMap.get(prevWord) + 1);
		Output.writeConsoleWhenTrue(word + "-" + wordsMap.get(word));
	}

	/*
	 * Joins the doc map to the total map
	 */
	private Map<String, Map<String, Integer>> listMapToMapTotalWordCount(List<Map<String, Map<String,Integer>>> docsList) 
	{
		int count = 0;
		Map<String, Map<String, Integer>> wordsCountMap = new TreeMap<String, Map<String, Integer>>();
		for(Map<String, Map<String, Integer>> docMap : docsList){
			joinMaps(wordsCountMap,docMap);		
			count++;

			System.out.println("Precentage " + (double)count/docsList.size() * 100); 
		}

		return wordsCountMap;
	}

	private void joinMaps(Map<String, Map<String, Integer>> wordsCountMap, Map<String, Map<String, Integer>> docMap){
		for (String word : docMap.keySet()){
			joinMapValues(wordsCountMap,docMap,word);
		}
	}

	/*
	 * Join key from 2 maps
	 */
	private void joinMapValues(Map<String, Map<String, Integer>> wordsCountMap, Map<String, Map<String, Integer>> docMap, String key)
	{
		Map<String, Integer> wordMap = wordsCountMap.get(key);

		if (wordMap == null)
		{
			Map<String, Integer> newWordMap = new TreeMap<String, Integer>();

			for (String word : docMap.get(key).keySet())
			{
				newWordMap.put(word, docMap.get(key).get(word));
			}

			wordsCountMap.put(key, newWordMap);
		}
		else
		{
			for (String word : docMap.get(key).keySet())
			{
				int wordApprearances = docMap.get(key).get(word);
				wordMap.put(word, wordMap.get(word) == null ? wordApprearances : wordMap.get(word) + wordApprearances);				
			}
		}

		Output.writeConsoleWhenTrue(key + "-" + wordsCountMap.get(key));
	}

	public void splitXPrecentOfDocsWords(double d, Map<String, Map<String, Integer>> firstXPrecentWordsMap, 
			Map<String, Map<String, Integer>> lastXPrecentWordsMap) 
	{
		if(d<0||d>1){
			System.out.println("Precent should be between 0 to 1");
			return;
		}

		// number of words in the first X precent
		long numFirstXPrecent = Math.round(d*totalWordsInDocs);
		Output.writeConsoleWhenTrue("Precent of the words is "+numFirstXPrecent);

		long count=0;
		int index=0;
		boolean xPrecentPasted = false;

		while(count < numFirstXPrecent && !xPrecentPasted){
			Map<String, Map<String, Integer>> currentDocMap = this.docsMapList.get(index);
			long numWordsInDoc = wordsTotalAmount(currentDocMap);

			// join the next doc (in case we don't pass X precent with the doc)
			if(count+numWordsInDoc<=numFirstXPrecent){
				joinMaps(firstXPrecentWordsMap,currentDocMap);	
				count+=numWordsInDoc;
			}
			else{
				// If splitting the doc is needed, 
				// run over the doc in which we pass the X percent by the order of the words in the doc
				String currentDocString = this.docsStringList.get(index);
				String[] words = currentDocString.split(" ");

				String prevWord = FirstArticleWord;

				for(String word : words)
				{					
					if(!xPrecentPasted && count < numFirstXPrecent)
					{
						// add word to map
						AddWordToMap(firstXPrecentWordsMap, word, prevWord);
						
						count++;
					}
					else
					{
						// passed x precent, start adding words to last set of words (validation)
						if (xPrecentPasted == false)
						{
							xPrecentPasted = true;
							prevWord = FirstArticleWord;
						}
						
						AddWordToMap(lastXPrecentWordsMap, word, prevWord);
					}

					prevWord = word;
				}
			}

			index++;
		}

		//After X precent, add all docs to the validation map (last set of words)
		while(index < this.docsMapList.size())
		{
			Map<String, Map<String, Integer>> currentDocMap = this.docsMapList.get(index++);
			joinMaps(lastXPrecentWordsMap, currentDocMap);	
		}
	}

	private Map<String, Map<String, Integer>> mapTotalDocsWordCount(List<Map<String, Map<String, Integer>>> docsMapList) {

		return listMapToMapTotalWordCount(docsMapList);
	}

	/*
	 * Returns the number of words in the map
	 */
	public static long wordsTotalAmount(Map<String, Map<String, Integer>> mapTotalDocsWords)
	{
		int count = 0;

		for(Map<String, Integer> map :  mapTotalDocsWords.values())
		{
			for (int value : map.values())
			{
				count += value;	
			}
		}

		return count;
	}

	public static long wordsTotalAmountReg(Map<String, Integer> wordsCountMap)
	{
		long count=0;

		for(int value :  wordsCountMap.values())
		{
			count += value;
		}

		return count;
	}
	public static void trainMapPrevWordCount(Map<String, Map<String, Integer>> lidstoneTrainMap)
	{
		for (String word : lidstoneTrainMap.keySet())
		{
			for (String prevWord : lidstoneTrainMap.get(word).keySet())
			{
				int prevWordOccurreneces = lidstoneTrainMap.get(word).get(prevWord);
				
				trainMapNotLastWordCount.put(prevWord, trainMapNotLastWordCount.get(prevWord) == null ? prevWordOccurreneces : trainMapNotLastWordCount.get(prevWord) + prevWordOccurreneces);
			}
		}
		
		trainMapNotLastWordCount.remove(FirstArticleWord);	
	}
	
	public static void CountWordOccurrencesInTrainMap(Map<String, Map<String, Integer>> lidstoneTrainMap)
	{
		for (String word : lidstoneTrainMap.keySet())
		{
			// sum all occurrences of the word (by the prev words set)
			trainMapWordCount.put(word, wordsTotalAmountReg(lidstoneTrainMap.get(word)));
		}
	}
	
	public static void trainMapCalcLidstonUnigram(Map<String, Map<String, Integer>> lidstoneTrainMap, long trainingMapSize)
	{
		for (String word : lidstoneTrainMap.keySet())
		{
			trainMapLidstonUnigram.put(word, LidstoneModel.CalcUnigramPLidstone(BackOff.UNIGRAM_LAMDA, lidstoneTrainMap, trainingMapSize, word));
		}
		
		trainMapLidstonUnigram.put(UNSEEN_WORD, LidstoneModel.CalcUnigramPLidstone(BackOff.UNIGRAM_LAMDA, lidstoneTrainMap, trainingMapSize, UNSEEN_WORD));

		trainMapLidstonUnigram.remove(FirstArticleWord);	
	}

	/*
	 * Gets the Total number of occurrences of word in map
	 */
	public static long getWordOccurrences(Map<String, Map<String, Integer>> map, String word)
	{
		if (trainMapWordCount.get(word) == null)
		{
			return 0;
		}
		
		return trainMapWordCount.get(word);
	}
	
	public static int getWordOccurrences(Map<String, Map<String, Integer>> map, String word, String prevWord)
	{
		if (map.get(word) == null)
		{
			return 0;
		}

		return map.get(word).get(prevWord) == null ? 0 : map.get(word).get(prevWord); 
	}
	
	public Map<String, Map<String, Integer>> getMapTotalDocsWords() 
	{
		return mapTotalDocsWords;
	}

	public long getTotalWordsInDocs() {
		return totalWordsInDocs;
	}

	public List<Set<Topics>> getDocsTopicList() {
		return docsTopicList;
	}

	public List<Map<String, Map<String, Integer>>> getDocsList() {
		return docsMapList;
	}

	public void setDocsList(List<Map<String, Map<String, Integer>>> docsList) {
		this.docsMapList = docsList;
	}

	public void setDocsTopicList(List<Set<Topics>> docsTopicList) {
		this.docsTopicList = docsTopicList;
	}
}
