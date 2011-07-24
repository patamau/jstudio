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
	private Set<Treatment> treatments;
	
	public Invoice(){
		
	}
	
	public Invoice(Date date, Person person, Set<Treatment> treatments){
		this.date=date;
		this.person=person;
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

	@Override
	public Long getId() {
		return id;
	}

}
