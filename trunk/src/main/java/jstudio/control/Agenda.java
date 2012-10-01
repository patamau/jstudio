package jstudio.control;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import jstudio.JStudio;
import jstudio.model.Event;
import jstudio.util.Configuration;

/**
 * Agenda holds events on a daily basis.
 * @author Matteo
 *
 */
public class Agenda extends Controller<Event> {
	
	private static final Logger logger = Logger.getLogger(Agenda.class);
	
	public static final String PRUNE_DAYS_KEY = "prune.days";
	public static final int PRUNE_DAYS_DEF = -2;
	
	public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final Calendar calendar = Calendar.getInstance();

	public Agenda(JStudio app){
		super(app, Event.class);
	}
	
	@SuppressWarnings("unchecked")
	public List<Event> getByDate(Date date){
		String from = dayDateFormat.format(date);
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		String to = dayDateFormat.format(calendar.getTime());
		return (List<Event>)getApplication().getDatabase().getBetween(getSource(), "date", from, to);
	}
	
	public int countAllToPrune(final Date date){
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, Configuration.getGlobal(PRUNE_DAYS_KEY, PRUNE_DAYS_DEF));
		final Date p = calendar.getTime();
		try{
			return (Integer)getApplication().getDatabase().executeQuery("SELECT COUNT(id) FROM "+source+" WHERE date<'"+dayDateFormat.format(p)+"'");
		}catch(Exception e){
			return 0;
		}
	}
	
	public boolean removeAllBefore(final Date date){
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, Configuration.getGlobal(PRUNE_DAYS_KEY, PRUNE_DAYS_DEF));
		final Date p = calendar.getTime();
		try {
			getApplication().getDatabase().execute("DELETE FROM "+source+" WHERE date<'"+dayDateFormat.format(p)+"'");
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
}
