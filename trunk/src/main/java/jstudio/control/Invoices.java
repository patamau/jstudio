package jstudio.control;

import java.util.List;

import jstudio.db.DatabaseInterface;
import jstudio.model.Event;
import jstudio.model.Invoice;

public class Invoices {

	public static String DB_TABLE = "Invoice";
	
	private DatabaseInterface database;

	public Invoices(DatabaseInterface dbmanager){
		database = dbmanager;
	}
	
	public Invoice getInvoice(int id){
		return (Invoice)database.get(DB_TABLE, id);
	}
	
	public Invoice addInvoice(Invoice e){
		return (Invoice)database.store(DB_TABLE, e);
	}
	
	@SuppressWarnings("unchecked")
	public List<Invoice> getAll(){
		return (List<Invoice>)database.getAll(DB_TABLE);
	}
}
