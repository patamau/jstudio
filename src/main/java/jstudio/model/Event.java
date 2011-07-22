package jstudio.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jstudio.db.DatabaseObject;

public class Event implements DatabaseObject {
	
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	private Long id;
	private Date date;
	private Person person;
	private String altPerson;
	private String description;
	
	public Event(){
	
	}

	public Event(Date date, Person person, String altPerson, String description){
		this.id=0l;
		this.date = date;
		this.person = person;
		this.altPerson = altPerson;
		this.description = description;
	}
	
	public Long getId(){
		return id;
	}
	
	public Date getDate() throws ParseException{
		return date;
	}
	
	public Person getPerson(){
		return person;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getAltPerson() {
		return altPerson;
	}

	public void setAltPerson(String altPerson) {
		this.altPerson = altPerson;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public String toString(){
		return timeFormat.format(date);
	}
}
