package jstudio.control;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jstudio.JStudio;
import jstudio.model.Event;

/**
 * Agenda holds events on a daily basis.
 * @author Matteo
 *
 */
public class Agenda extends Controller<Event> {
	
	public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public Agenda(JStudio app){
		super(app, Event.class.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<Event> getByDate(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, 1);
		String from = dayDateFormat.format(date);
		String to = dayDateFormat.format(c.getTime());
		return (List<Event>)getApplication().getDatabase().getBetween(getSource(), "date", from, to);
	}
}
