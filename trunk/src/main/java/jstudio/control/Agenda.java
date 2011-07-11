package jstudio.control;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jstudio.db.DatabaseInterface;
import jstudio.model.Event;
import jstudio.model.Person;

/**
 * Agenda holds events on a daily basis.
 * @author Matteo
 *
 */
public class Agenda {
	
	public static String DB_TABLE = "Event";
	public static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
	public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private DatabaseInterface database;

	public Agenda(DatabaseInterface dbmanager){
		database = dbmanager;
	}
	
	public Event getEvent(int id){
		return (Event)database.get(DB_TABLE, id);
	}
	
	public Event addEvent(Event e){
		return (Event)database.store(DB_TABLE, e);
	}
	
	@SuppressWarnings("unchecked")
	public List<Event> getAll(){
		return (List<Event>)database.getAll(DB_TABLE);
	}
}
