package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import jstudio.db.DatabaseObject;

public class Invoice implements DatabaseObject{
	
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
	
	//FIXME: used for debuggin
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(Person.birthdateFormat.format(date));
		sb.append(" ");
		sb.append(person.getName());
		sb.append(" ");
		sb.append(person.getLastname());
		for(Treatment t: treatments){
			sb.append(" ");
			sb.append(t);
		}
		return sb.toString();
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCap() {
		return cap;
	}

	public void setCap(String cap) {
		this.cap = cap;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
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
