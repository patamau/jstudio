package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jstudio.db.DatabaseObject;


public class Person implements DatabaseObject, Comparable<Object> {

	private static final long serialVersionUID = 980515156789194219L;
	
	public static final SimpleDateFormat birthdateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public enum Gender{
		Male("M",0),
		Female("F",1);
		
		private int id;
		private String str;
		
		private Gender(final String str, final int id){
			this.id=id;
			this.str=str;
		}
		
		public String toString(){
			return str;
		}
		
	    public int getId() {
	        return id;
	    }
	    
	    public static Gender getGender(final int id){
	    	return id==0?Male:Female;
	    }
	}
	
	private Long id;
	private Date birthdate;
	private String name, lastname,		
		address, city, province, cap, 
		code, phone;
	private Integer gender;
	
	public Person(){
		this(0l);
	}
	
	public Person(Long id){
		this.id=id;
		birthdate=new Date();
		name="";
		lastname="";
		address="";
		city="";
		province="";
		cap="";
		code="";
		phone="";
		gender=Gender.Male.getId();
	}
	
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
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
			return p.lastname.compareTo(lastname);
		}
		return 1;
	}
	

	public String toString(){
		return lastname;
	}

	@Override
	public Map<String, String> getPrintData() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("name",name);
		map.put("lastname",lastname);
		map.put("gender", Gender.getGender(gender).toString());
		map.put("address",address);
		map.put("city",city);
		map.put("province", province);
		map.put("cap",cap);
		map.put("code", code);
		map.put("phone", phone);
		map.put("birthdate", Person.birthdateFormat.format(birthdate));
		return map;
	}	
}
