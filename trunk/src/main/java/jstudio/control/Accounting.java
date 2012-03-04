package jstudio.control;

import java.util.Date;
import java.util.Calendar;

import jstudio.JStudio;
import jstudio.model.Invoice;
import jstudio.model.Product;

public class Accounting extends Controller<Invoice>{
	
	private Controller<Product> products;

	public Accounting(JStudio app){
		super(app, Invoice.class);
		this.products = new Controller<Product>(app, Product.class);
	}
	
	public Controller<Product> getProducts(){
		return products;
	}
	
	public Long getNextInvoiceNumber(){
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		Integer y = c.get(Calendar.YEAR);
		String query = "SELECT COUNT(id) FROM "+source;
		query += " WHERE date>='01/01/"+y+"' AND date<='31/12/"+y+"'";
		System.err.println("Executing query "+query);
		Object o = app.getDatabase().executeQuery(query);
		System.err.println("Number is "+o);
		Long number;
		if(o!=null){
			number = ((Long)o)+1l;
		}else{
			number = 1l;
		}
		return number;
	}
}
