package jstudio.model;

import jstudio.db.DatabaseObject;

public class Treatment implements DatabaseObject{

	private Long id;
	private Invoice invoice;
	private String description;
	private Float cost;
	
	public Treatment(){
		
	}
	
	public Treatment(Invoice invoice, String description, Float cost){
		this.id=0l;
		this.invoice=invoice;
		this.description=description;
		this.cost=cost;
	}
	
	//FIXME: only for debugging 
	public String toString(){
		return description+" "+cost;
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
		this.cost = cost;
	}

	@Override
	public Long getId() {
		return id;
	}

}
