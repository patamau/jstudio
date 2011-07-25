package jstudio.control;

import java.text.SimpleDateFormat;

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
}
