/* Ido Cohen	Guy Cohen	203516992	304840283 */
package InputOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class DataClass {

	private boolean skipLine = true;
	
	private List<Set<Topics>> docsTopicList;
	private List<Map<String,Integer>> docsMapList;
	private List<String> docsStringList;
	
	private Map<String,Integer> mapTotalDocsWords;
	private long totalWordsInDocs;
	

	public DataClass(){
		this.docsTopicList = new ArrayList<Set<Topics>>();
		this.docsMapList = new ArrayList<Map<String,Integer>>();
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
			docsMapList.add(mapWordCount(docTextLine));
			docsStringList.add(docTextLine);

			skipEmptyLine(bufferedReader);

		}
		fileReader.close();

		mapTotalDocsWordCount();
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
	private Map<String, Integer> mapWordCount(String inputLine) {

		Map<String, Integer> wordsMap = new TreeMap<String, Integer>();
		String[] words = inputLine.split(" ");
		for(String word : words){
			word = word.toLowerCase();
			wordsMap.put(word, wordsMap.get(word) == null ? 1 : wordsMap.get(word)+1);
			Output.writeConsoleWhenTrue(word + "-" + wordsMap.get(word));
		}
		return wordsMap;
	}

	/*
	 * Joins the doc map to the total map
	 */
	private Map<String, Integer> listMapToMapTotalWordCount(List<Map<String,Integer>> docsList) {
		Map<String, Integer> wordsCountMap = new TreeMap<String, Integer>();
		for(Map<String, Integer> docMap : docsList){
			joinMaps(wordsCountMap,docMap);		
		}
		return wordsCountMap;
	}

	private void joinMaps(Map<String, Integer> srcMap, Map<String, Integer> otherMap){
		for (String word : otherMap.keySet()){
			joinMapValues(srcMap,otherMap,word);
		}
	}

	/*
	 * Join key from 2 maps
	 */
	private void joinMapValues(Map<String, Integer> srcMap, Map<String, Integer> otherMap, String key){
		srcMap.put(key, srcMap.get(key) == null ? otherMap.get(key) : srcMap.get(key)+otherMap.get(key));
		Output.writeConsoleWhenTrue(key + "-" + srcMap.get(key));
	}

	public void splitXPrecentOfDocsWords(double d, Map<String, Integer> firstXPrecentWordsMap, Map<String, Integer> lastXPrecentWordsMap) 
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
		
		while(count<numFirstXPrecent && !xPrecentPasted){
			Map<String, Integer> currentDocMap = this.docsMapList.get(index);
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
				for(String word : words){
					word = word.toLowerCase();
					
					if(!xPrecentPasted && count<numFirstXPrecent)
					{
						// add word to map
						firstXPrecentWordsMap.put(word, firstXPrecentWordsMap.get(word) == null ? 1 : firstXPrecentWordsMap.get(word)+1);
						Output.writeConsoleWhenTrue(word + "-" + firstXPrecentWordsMap.get(word));
						count++;
					}
					else
					{
						// passed x precent, start adding words to last set of words (validation)
						xPrecentPasted = true;
						lastXPrecentWordsMap.put(word, lastXPrecentWordsMap.get(word) == null ? 1 : lastXPrecentWordsMap.get(word)+1);
						Output.writeConsoleWhenTrue("AfterXPrecent: " + word + "-" + lastXPrecentWordsMap.get(word));
					}
				}
			}

			index++;
		}
		
		//After X precent, add all docs to the validation map (last set of words)
		while(index<this.docsMapList.size()){
			Map<String, Integer> currentDocMap = this.docsMapList.get(index++);
			joinMaps(lastXPrecentWordsMap,currentDocMap);	
		}
	}

	private void mapTotalDocsWordCount() {

		mapTotalDocsWords = listMapToMapTotalWordCount(this.docsMapList);
	}
	
	/*
	 * Returns the number of words in the map
	 */
	public static long wordsTotalAmount(Map<String, Integer> wordsCountMap)
	{
		int count=0;

		for(int value :  wordsCountMap.values())
		{
			count += value;
		}

		return count;
	}

	public Map<String, Integer> getMapTotalDocsWords() 
	{
		return mapTotalDocsWords;
	}
	
	public long getTotalWordsInDocs() {
		return totalWordsInDocs;
	}
	
	public List<Set<Topics>> getDocsTopicList() {
		return docsTopicList;
	}

	public List<Map<String, Integer>> getDocsList() {
		return docsMapList;
	}

	public void setDocsList(List<Map<String, Integer>> docsList) {
		this.docsMapList = docsList;
	}

	public void setDocsTopicList(List<Set<Topics>> docsTopicList) {
		this.docsTopicList = docsTopicList;
	}
}
