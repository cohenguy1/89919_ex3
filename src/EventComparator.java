/* Ido Cohen	Guy Cohen	203516992	304840283 */
import java.util.Comparator;

public class EventComparator implements Comparator<Event>
{
	@Override
	public int compare(Event event1, Event event2) 
	{
		return Double.compare(event2.probability, event1.probability);
	}
}