/* Ido Cohen	Guy Cohen	203516992	304840283 */
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