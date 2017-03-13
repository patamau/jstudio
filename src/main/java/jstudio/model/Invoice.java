package jstudio.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jstudio.db.DatabaseObject;
import jstudio.gui.InvoicePanel;
import jstudio.report.ReportGenerator;
import jstudio.util.Configuration;
import jstudio.util.Language;

public class Invoice implements DatabaseObject, Comparable<Invoice> {
	
	private static final long serialVersionUID = 2636840744342729819L;

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public static final String 
		INVOICE_STAMP_THRESHOLD = "invoice.stamp.threshold",
		INVOICE_STAMP_VALUE = "invoice.stamp.value";
	public static final float
		INVOICE_STAMP_THRESHOLD_DEF = 77.48f,
		INVOICE_STAMP_VALUE_DEF = 1.81f;

	private Long id, number;
	private Date date;
	private String name, lastname, address, city, province, cap, code, note, privacy;
	private Float stamp;
	private List<Product> products;
	private transient boolean modified;
	
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
		this.note="";
		this.privacy="";
		this.stamp=0f;
		this.products = new LinkedList<Product>();
	}
	
	public void setModified(final boolean modified){
		this.modified = modified;
	}
	
	public boolean isModified(){
		return modified;
	}
	
	public String getPrivacy(){
		return privacy;
	}
	
	public void setPrivacy(final String privacy){
		this.privacy = privacy;
		System.out.println("privacy set to "+privacy);
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(final String note) {
		this.note = note;
	}

	public float getStamp() {
		return stamp;
	}

	public void setStamp(final float stamp) {
		this.stamp = stamp;
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
	
	public void updateStamp(){
		final float threshold = Configuration.getGlobal(Invoice.INVOICE_STAMP_THRESHOLD, Invoice.INVOICE_STAMP_THRESHOLD_DEF);
		final float total = getTotal();
		if(total>threshold){
			final float stamp = Configuration.getGlobal(Invoice.INVOICE_STAMP_VALUE, Invoice.INVOICE_STAMP_VALUE_DEF);
			setStamp(stamp);
		}else{
			setStamp(0f);
		}
	}
	
	/** Compute and return the sum of product cost **/
	public float getTotal(){
		float total = 0f;
		for(Product t: products){
			total += t.getQuantity()*t.getCost();
		}
		total += stamp;
		return total;
	}
	
	/** Number/Year **/
	public String getInvoiceId(){
		Calendar c = Calendar.getInstance();
		c.setTime(getDate());
		Integer y = c.get(Calendar.YEAR);
		return number+"/"+y;
	}
	
	/** Year_Number **/
	public String getFilePrefix(){
		Calendar c = Calendar.getInstance();
		c.setTime(getDate());
		Integer y = c.get(Calendar.YEAR);
		//%[argument_index$][flags][width][.precision]conversion
		return String.format("%1$4d_%2$03d", y, number); 
		//return Integer.toString(y)+"_"+String.format("number);
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

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(final List<Product> products) {
		this.products = new LinkedList<Product>(products);
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
	}

	@Override
	public Map<String, String> getPrintData() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("number", Long.toString(number));
		map.put("date", dateFormat.format(date));
		map.put("name", name);
		map.put("lastname", lastname);
		map.put("code",code);
		map.put("address",address);
		map.put("cap",cap);
		map.put("city",city);
		map.put("province",province);
		map.put("stamp", Product.formatCurrency(stamp));
		map.put("note", Language.string(note));
		map.put("privacy", Language.string(privacy));
		return map;
	}
	
	/**
	 * Configures the report data
	 * @param rg
	 */
	public void setReport(ReportGenerator rg){
		rg.setReport(Configuration.getGlobal(InvoicePanel.INVOICE_REPORT, InvoicePanel.INVOICE_REPORT_DEF));
		rg.setHead(this);
		rg.setHeadValue("date", Person.birthdateFormat.format(this.getDate()));
		rg.setData(this.getProducts());
		//have to set those fields again for the footer!
		rg.setHeadValue("stamp", Product.formatCurrency(this.getStamp()));
		rg.setHeadValue("totalcost", Product.formatCurrency(this.getTotal()));
		rg.setHeadValue("privacy", Language.string(this.getPrivacy()));
	}
}
