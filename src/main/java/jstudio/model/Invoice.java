package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import jstudio.db.DatabaseObject;

public class Invoice implements DatabaseObject{
	
	private static final long serialVersionUID = 2636840744342729819L;

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	private Long id;
	private Date date;
	private Person person;
	private String name, lastname, address, city, cap, code;
	private Set<Treatment> treatments;
	
	public Invoice(){
		
	}

	public Invoice(Date date, String name, String lastname, String address, String city, String cap, String code, Set<Treatment> treatments){
		this.date=date;
		this.name=name;
		this.lastname=lastname;
		this.address=address;
		this.city=city;
		this.cap=cap;
		this.code=code;
		this.treatments=treatments;
	}
	
	public Invoice(Date date, Person person, Set<Treatment> treatments){
		this.date=date;
		this.person=person;
		this.name=person.getName();
		this.lastname=person.getLastname();
		this.address=person.getAddress();
		this.city=person.getCity();
		this.cap=person.getCap();
		this.code=person.getCode();
		this.treatments=treatments;
	}
	
	public String toString(){
		return Long.toString(id);
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		if(address==null) address = "";
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		if(city==null) city = "";
		this.city = city;
	}

	public String getCap() {
		return cap;
	}

	public void setCap(String cap) {
		if(cap==null) cap = "";
		this.cap = cap;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		if(cap==null) cap = "";
		this.code = code;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Collection<Treatment> getTreatments() {
		return treatments;
	}

	public void setTreatments(Set<Treatment> treatments) {
		this.treatments = treatments;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

}
