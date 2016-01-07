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
	public static String BEGIN_ARTICLE = "begin-article";
	private boolean skipLine = true;

	private List<Set<Topics>> docsTopicList;

	private List<Map<String,Map<String, Integer>>> docsMapPrevList;

	private List<Document> docsList;

	public static Map<String, Map<String, Integer>> TrainingMap = new TreeMap<String, Map<String, Integer>>();
	public static Map<String, Map<String, Integer>> SequentialMap = new TreeMap<String, Map<String, Integer>>();
	
	public static Map<String, Map<String, Integer>> TestMap = new TreeMap<String, Map<String, Integer>>();
	
	private long totalWordsInDocsWithoutBeginArticle;
	
	public static Map<String, Integer> WordCountMapInTrainThatHavePrev = new HashMap<String, Integer>();
	
	public static Map<String, Integer> wordCountMapInTrainThatAreNotLastWithBeginArticle = new HashMap<String, Integer>();
	public static Map<String, Double> trainMapLidstonUnigram = new HashMap<String, Double>();


	public DataClass(){
		this.docsTopicList = new ArrayList<Set<Topics>>();
		this.docsList = new ArrayList<Document>();
	}

	/*
	 * Parse the input file
	 */
	public void readInputFile(String inputFile) throws IOException{

		Output.writeConsoleWhenTrue(Output.folderPath+inputFile);

		FileReader fileReader = new FileReader(Output.folderPath+inputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String docTopicLine;
		totalWordsInDocsWithoutBeginArticle = 0;

		while ((docTopicLine = bufferedReader.readLine()) != null) {

			docsTopicList.add(setTopicFromLine(docTopicLine));

			skipEmptyLine(bufferedReader);

			String docTextLine = bufferedReader.readLine();
			long wordCount = wordCount(docTextLine);
			
			docsList.add(new Document(docTextLine, wordCount));

			totalWordsInDocsWithoutBeginArticle += wordCount;
			
			skipEmptyLine(bufferedReader);

		}
		
		fileReader.close();
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
	private long wordCount(String inputLine) 
	{
		String[] words = inputLine.split(" ");
		return words.length;
	}
	
	/*
	 * Adds each word of the line read to the word mapping 
	 */
	private void mapWordCount(String inputLine) 
	{
		String[] words = inputLine.split(" ");
		String prevWord = BEGIN_ARTICLE;

		AddWordToMap(TrainingMap, BEGIN_ARTICLE, "");
		
		for(String word : words)
		{
			AddWordToMap(TrainingMap, word, prevWord);
			AddToSequentialMap(SequentialMap, word, prevWord);

			prevWord = word;
		}
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

	public void splitXPrecentOfDocsWords(double d, Map<String, Map<String, Integer>> validationMap) 
	{
		if(d < 0||d > 1)
		{
			System.out.println("Precent should be between 0 to 1");
			return;
		}

		// number of words in the first X precent
		long numFirstXPrecent = Math.round(d*totalWordsInDocsWithoutBeginArticle);
		Output.writeConsoleWhenTrue("Precent of the words is "+numFirstXPrecent);

		long count = 0;
		int index = 0;
		boolean xPrecentPasted = false;

		String[] words;
		String prevWord;
		
		while(count < numFirstXPrecent && !xPrecentPasted)
		{
			Document currentDoc = this.docsList.get(index);
			
			// join the next doc (in case we don't pass X precent with the doc)
			if(count + currentDoc.wordCount <= numFirstXPrecent)
			{
				mapWordCount(currentDoc.content);
				count += currentDoc.wordCount;
			}
			else{
				// If splitting the doc is needed, 
				// run over the doc in which we pass the X percent by the order of the words in the doc
				words = currentDoc.content.split(" ");

				prevWord = BEGIN_ARTICLE;
				AddWordToMap(TrainingMap, BEGIN_ARTICLE, "");

				for(String word : words)
				{					
					if(!xPrecentPasted && count < numFirstXPrecent)
					{
						// add word to map
						AddWordToMap(TrainingMap, word, prevWord);
						AddToSequentialMap(SequentialMap, word, prevWord);
						
						count++;
					}
					else
					{
						// passed x precent, start adding words to last set of words (validation)
						if (xPrecentPasted == false)
						{
							xPrecentPasted = true;
							prevWord = BEGIN_ARTICLE;
							
							AddWordToMap(validationMap, BEGIN_ARTICLE, "");
						}
						
						AddWordToMap(validationMap, word, prevWord);
					}

					prevWord = word;
				}
			}

			index++;
		}

		//After X precent, add all docs to the validation map (last set of words)
		while(index < this.docsList.size())
		{
			Document currentDoc = this.docsList.get(index++);
			
			words = currentDoc.content.split(" ");
			prevWord = BEGIN_ARTICLE;

			AddWordToMap(validationMap, BEGIN_ARTICLE, "");
			
			for(String word : words)
			{					
				// add word to map
				AddWordToMap(validationMap, word, prevWord);
				
				prevWord = word;
			}
		}
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
	
	public static long wordsTotalAmountWithoutBeginArticle(Map<String, Map<String, Integer>> mapTotalDocsWords)
	{
		long totalAmount = wordsTotalAmount(mapTotalDocsWords);
		
		int beginArticleCount = mapTotalDocsWords.get(BEGIN_ARTICLE) == null ? 0 : wordsTotalAmountReg(mapTotalDocsWords.get(BEGIN_ARTICLE));

		return totalAmount - beginArticleCount;
	}
	
	public static int wordsTotalAmountReg(Map<String, Integer> wordsCountMap)
	{
		int count = 0;

		for (int value :  wordsCountMap.values())
		{
			count += value;
		}

		return count;
	}
	
	public static void keepWordCountMaptInTrainTharAreNotLast()
	{
		for (String word : TrainingMap.keySet())
		{
			for (String prevWord : TrainingMap.get(word).keySet())
			{
				int prevWordOccurreneces = TrainingMap.get(word).get(prevWord);
				
				wordCountMapInTrainThatAreNotLastWithBeginArticle.put(prevWord, wordCountMapInTrainThatAreNotLastWithBeginArticle.get(prevWord) == null ? prevWordOccurreneces : wordCountMapInTrainThatAreNotLastWithBeginArticle.get(prevWord) + prevWordOccurreneces);
			}
		}
	}
	
	public static void keepCountWordOccurrencesMapInTrainThatHavePrev()
	{
		for (String word : TrainingMap.keySet())
		{
			// sum all occurrences of the word (by the prev words set)
			WordCountMapInTrainThatHavePrev.put(word, wordsTotalAmountReg(TrainingMap.get(word)));
		}
	}
	
	public static void trainMapCalcLidstonUnigram(long trainingMapSize)
	{
		for (String word : TrainingMap.keySet())
		{
			trainMapLidstonUnigram.put(word, LidstoneModel.CalcUnigramPLidstone(BackOff.UNIGRAM_LAMDA, TrainingMap, trainingMapSize, word));
		}
		
		trainMapLidstonUnigram.put(UNSEEN_WORD, LidstoneModel.CalcUnigramPLidstone(BackOff.UNIGRAM_LAMDA, TrainingMap, trainingMapSize, UNSEEN_WORD));
	}

	/*
	 * Gets the Total number of occurrences of word in map
	 */
	public static long getWordOccurrences(Map<String, Map<String, Integer>> map, String word)
	{
		return WordCountMapInTrainThatHavePrev.get(word) == null ? 0 : WordCountMapInTrainThatHavePrev.get(word);
	}
	
	public static int getWordOccurrences(Map<String, Map<String, Integer>> map, String word, String prevWord)
	{
		if (map.get(word) == null)
		{
			return 0;
		}

		return map.get(word).get(prevWord) == null ? 0 : map.get(word).get(prevWord); 
	}

	public long getTotalWordsInDocsWithoutBeginArticle() {
		return totalWordsInDocsWithoutBeginArticle;
	}

	public List<Set<Topics>> getDocsTopicList() {
		return docsTopicList;
	}

	public List<Map<String, Map<String, Integer>>> getDocsList() {
		return docsMapPrevList;
	}

	public void setDocsList(List<Map<String, Map<String, Integer>>> docsList) {
		this.docsMapPrevList = docsList;
	}

	public void setDocsTopicList(List<Set<Topics>> docsTopicList) {
		this.docsTopicList = docsTopicList;
	}
	
	/*
	 * Parse the input file
	 */
	public long readTestFile(String inputFile) throws IOException{

		Output.writeConsoleWhenTrue(Output.folderPath+inputFile);

		FileReader fileReader = new FileReader(Output.folderPath+inputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String docTopicLine;
		long totalWordsWithoutBeginArticle = 0;

		while ((docTopicLine = bufferedReader.readLine()) != null) 
		{
			skipEmptyLine(bufferedReader);

			String docTextLine = bufferedReader.readLine();
			long wordCount = wordCount(docTextLine);

			totalWordsWithoutBeginArticle += wordCount;

			String[] words = docTextLine.split(" ");
			String prevWord = BEGIN_ARTICLE;

			AddWordToMap(TestMap, BEGIN_ARTICLE, "");
			
			for(String word : words)
			{
				AddWordToMap(TestMap, word, prevWord);

				prevWord = word;
			}
			
			skipEmptyLine(bufferedReader);
		}
		
		fileReader.close();
		
		return totalWordsWithoutBeginArticle;
	}
}
