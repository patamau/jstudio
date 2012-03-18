package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import jstudio.db.DatabaseObject;

public class Invoice implements DatabaseObject, Comparable<Invoice> {
	
	private static final long serialVersionUID = 2636840744342729819L;

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	private Long id, number;
	private Date date;
	private String name, lastname, address, city, province, cap, code;
	private Set<Product> products;
	
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}
	
	public Invoice(){
		this(0l);
	}
	
	public Invoice(Long id){
		this.id=id;
		this.date=new Date();
		this.number=0l;
		this.name="";
		this.lastname="";
		this.address="";
		this.city="";
		this.cap="";
		this.province="";
		this.code="";
		this.products = new HashSet<Product>();
	}
	
	public String toString(){
		return getInvoiceId();
	}
	
	public Long getNumber(){
		if(number==null) number=0l;
		return number;
	}
	
	public void setNumber(final Long number){
		this.number = number;
	}
	
	/** Number/Year **/
	public String getInvoiceId(){
		Calendar c = Calendar.getInstance();
		c.setTime(getDate());
		Integer y = c.get(Calendar.YEAR);
		return number+"/"+y;
	}
	
	/** Year_number **/
	public String getFilePrefix(){
		Calendar c = Calendar.getInstance();
		c.setTime(getDate());
		Integer y = c.get(Calendar.YEAR);
		return Integer.toString(y)+"."+Long.toString(number)+".";
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

	public void setCode(final String code) {
		if(cap==null) cap = "";
		this.code = code;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public Set<Product> getProducts() {
		return products;
	}

	public void setProducts(final Set<Product> products) {
		this.products = new TreeSet<Product>(products);
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	@Override
	public int compareTo(final Invoice o) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int y = c.get(Calendar.YEAR);
		c.setTime(o.date);
		int oy = c.get(Calendar.YEAR);
		return y<oy?-1:(y>oy?1:this.number<o.number?-1:(this.number>o.number?1:0));
		//return this.date.before(o.date)?-1:(this.date.after(o.date)?1:0);
		//return this.id==o.id?0:(this.id>o.id?1:-1);
	}
}
