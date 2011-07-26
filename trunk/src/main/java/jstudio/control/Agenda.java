package jstudio.control;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import jstudio.db.DatabaseInterface;
import jstudio.model.Event;

/**
 * Agenda holds events on a daily basis.
 * @author Matteo
 *
 */
public class Agenda extends Controller<Event> {
	
	public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public Agenda(DatabaseInterface database){
		super(database);
		setSource(Event.class.getName());
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Event> getByDate(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, 1);
		String from = dayDateFormat.format(date);
		String to = dayDateFormat.format(c.getTime());
		return (Collection<Event>)database.getBetween(getSource(), "date", from, to);
	}
}
