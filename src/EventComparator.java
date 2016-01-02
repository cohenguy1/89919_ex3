import java.util.Comparator;

public class EventComparator implements Comparator<Event>
{
	@Override
	public int compare(Event event1, Event event2) 
	{
		if (event1.probability >= event2.probability)
		{
			return 1;
		}
		
		return 0;
	}
}