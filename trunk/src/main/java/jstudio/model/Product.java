package jstudio.model;

import jstudio.db.DatabaseObject;

public class Product implements DatabaseObject{

	private static final long serialVersionUID = -3847404193905268779L;
	
	private Long id;
	private Invoice invoice;
	private String description;
	private Integer quantity;
	private Float cost;
	
	public Product(){
		
	}
	
	public Product(Invoice invoice, String description, Integer quantity, Float cost){
		this.id=0l;
		this.invoice=invoice;
		this.description=description;
		this.quantity=quantity!=null?quantity:0;
		this.cost=cost!=null?cost:0f;
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

}
