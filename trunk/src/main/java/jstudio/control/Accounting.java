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

	//TODO
	/**
	 * HAAAAAAAAAAAAAAAAAAAAAAA!!!!
	 */
	public Long _getNextInvoiceNumber(){
		//take special data from invoice 1
		Invoice data = this.get(1);
		if(data==null){
			data = new Invoice();
			data.setId(1l);
		}
		//compare date year with current year
		Calendar c = Calendar.getInstance();
		c.setTime(data.getDate());
		Integer dateYear = c.get(Calendar.YEAR);
		Date now = new Date();
		c.setTime(now);
		Integer currentYear = c.get(Calendar.YEAR);
		//if date year != current year
		if(dateYear!=currentYear){
			//set date to current year and reset number to 0
			data.setDate(now);
			data.setNumber(1l);
		}
		Long number = data.getNumber()+1l;
		//take number and increase by 1
		data.setNumber(number);
		//store updated data
		this.store(data);
		return number;
	}
	
	public Long getNextInvoiceNumber(){
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		Integer y = c.get(Calendar.YEAR);
		String query = "SELECT MAX(number) FROM "+source;
		query += " WHERE date>='01/01/"+y+"' AND date<='31/12/"+y+"'";
		System.err.println("Executing query "+query);
		Object o = app.getDatabase().executeQuery(query);
		System.err.println("Number is "+o);
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
