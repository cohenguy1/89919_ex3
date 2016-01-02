
public class Event 
{ 
	public final String word; 
	public final long occurrencesAfterInputWord;
	public final double probability;

	public Event(String word, long occ, double prob) 
	{ 
		this.word = word; 
		this.occurrencesAfterInputWord = occ;
		this.probability = prob;
	} 
}