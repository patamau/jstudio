package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import jstudio.db.DatabaseObject;

public class Event implements DatabaseObject {
	
	private static final long serialVersionUID = 715529267460671641L;

	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	private Long id;
	private Date date;
	private Person person;
	private String name, lastname, phone;
	private String description;
	
	public Event(){
	
	}
	
	public Event(Date date, String name, String lastname, String phone, String description){
		this.id=0l;
		this.date = date;
		this.person = null;
		this.name = name;
		this.lastname = lastname;
		this.phone = phone;
		this.description = description;
	}

	public Event(Date date, Person person, String description){
		this.id=0l;
		this.date = date;
		this.person = person;
		this.name = person.getName();
		this.lastname = person.getLastname();
		this.phone = person.getAddress();
		this.description = description;
	}
	
	public Long getId(){
		return id;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public Date getDate(){
		return date;
	}
	
	public Person getPerson(){
		return person;
	}
	
	public String getDescription(){
		return description;
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