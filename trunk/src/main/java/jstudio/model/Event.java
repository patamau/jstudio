package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jstudio.db.DatabaseObject;

public class Event implements DatabaseObject {
	
	private static final long serialVersionUID = 715529267460671641L;

	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	private Long id;
	private Date date;
	private String name, lastname, phone;
	private String description;
	
	public Event(){
		id = 0l;
	}
	
	public Event(Long id){
		this.id = id;
	}
	
	public Event(Date date, String name, String lastname, String phone, String description){
		this.id=0l;
		this.date = date;
		this.name = name;
		this.lastname = lastname;
		this.phone = phone;
		this.description = description;
	}

	public Event(Date date, Person person, String description){
		this.id=0l;
		this.date = date;
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
		if(name==null) name = "";
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		if(lastname==null) lastname = "";
		this.lastname = lastname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		if(phone==null) phone = "";
		this.phone = phone;
	}
	
	public Date getDate(){
		if(date==null) date = new Date();
		return date;
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

	public void setDescription(String description) {
		if(description==null) description="";
		this.description = description;
	}	
	
	public String toString(){
		return timeFormat.format(date);
	}

	@Override
	public Map<String, String> getPrintData() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("date", timeFormat.format(date));
		map.put("name", name);
		map.put("lastname", lastname);
		map.put("phone", phone);
		map.put("description", description);
		return map;
	}
}
