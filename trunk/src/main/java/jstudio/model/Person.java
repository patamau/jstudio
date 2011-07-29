package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import jstudio.db.DatabaseObject;


public class Person implements DatabaseObject, Comparable<Object> {

	private static final long serialVersionUID = 980515156789194219L;
	
	public static final SimpleDateFormat birthdateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public enum Gender{
		Male(0),
		Female(1);
		
		private int id;
		
		private Gender(int id){
			this.id=id;
		}
		
	    public int getId() {
	        return id;
	    }
	}
	
	private Long id;
	private Date birthdate;
	private String name, lastname, address, city, cap, code, phone;
	private Integer gender;
	
	public Person(){
		
	}

	public Person(String name, String lastname, Date birthdate, String address, String city, String cap, String code, String phone){
		this.id=0l;
		this.name=name;
		this.lastname=lastname;
		this.birthdate=birthdate;
		this.address=address;
		this.phone=phone;
	}
	
	public Integer getGender(){
		if(gender==null) gender=Gender.Male.getId();
		return gender;
	}
	
	public void setGender(Integer gender){
		this.gender = gender;
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

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
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
	
	public Date getBirthdate(){
		return birthdate;
	}
	
	public String getAddress(){
		return address;
	}
	
	public String getPhone(){
		return phone;
	}
	
	public String toString(){
		return Long.toString(id);
	}	
	
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public boolean equals(Object o){
		if(o instanceof Person){
			Person p = (Person)o;
			return p.id==id;
		}
		return false;
	}
	
	public int compareTo(Object o){
		if(o instanceof Person){
			Person p = (Person)o;
			if(p.id==id) return 0;
			if(p.id<id) return -1;
		}
		return 1;
	}
}
