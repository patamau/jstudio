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
		String query = "SELECT MAX(number) FROM "+source;
		query += " WHERE date>='01/01/"+y+"' AND date<='31/12/"+y+"'";
		Object o = app.getDatabase().executeQuery(query);
		Long number;
		if(o!=null){
			if(o instanceof Long){
				number = ((Long)o)+1l;
			}else if(o instanceof Integer){
				number = ((Integer)o).longValue()+1l;
			}else{
				number = 1l;
			}
		}else{
			number = 1l;
		}
		return number;
	}
}
