package jstudio.control;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jstudio.db.DatabaseInterface;
import jstudio.model.Person;

/**
 * Rubrica
 * @author Matteo
 *
 */
public class Contacts {
	
	public static String DB_TABLE = "Person";
	
	private DatabaseInterface database;
	
	public Contacts(DatabaseInterface dbmanager){
		database = dbmanager;
	}

	/**
	 * Retrieve a Person object given its id
	 * @param id
	 * @return
	 */
	public Person getPerson(int id){
		return (Person)database.get(DB_TABLE, id);
	}
	
	public Person addPerson(Person p){
		return (Person)database.store(DB_TABLE, p);
	}

	public List<Person> getAll() {
		try{
			return (List<Person>)database.getAll(DB_TABLE);
		}catch(Exception e){			
			return new ArrayList<Person>();
		}
	}
}
