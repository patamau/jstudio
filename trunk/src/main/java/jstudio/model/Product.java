package jstudio.model;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import jstudio.db.DatabaseObject;

public class Product implements DatabaseObject, Comparable<Product> {

	private static final long serialVersionUID = -3847404193905268779L;
	
	private Long id;
	private Invoice invoice;
	private String description;
	private Integer quantity;
	private Float cost;
	
	public Product(){
		this(0l);
	}
	
	public Product(Long id){
		this(id, null);
	}
	
	public Product(Long id, Invoice invoice){
		this.id=id;
		this.invoice=invoice;
		this.description="";
		this.quantity=0;
		this.cost=0f;
	}
	
	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		if(quantity==null) quantity=1;
		this.quantity = quantity;
	}

	public String toString(){
		return description;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Float getCost() {
		return cost;
	}

	public void setCost(Float cost) {
		if(cost==null) cost = 0f;
		this.cost = cost;
	}

	public Long getId() {
		return id;
	}

	public int compareTo(Product o) {
		if(o.id==0||this.id==0) return -1;		
		return id.compareTo(o.id);
	}
	
	public Map<String,String> getPrintData(){
		Map<String,String> data = new HashMap<String,String>();
		data.put("description", description);
		data.put("cost", NumberFormat.getCurrencyInstance().format(cost));
		data.put("quantity", Integer.toString(quantity));
		return data;
	}
}
