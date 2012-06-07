package jstudio.control;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import jstudio.JStudio;
import jstudio.model.Event;
import jstudio.model.Invoice;
import jstudio.model.Product;

public class Accounting extends Controller<Invoice>{
	
	private static final Logger logger = Logger.getLogger(Accounting.class);
	
	public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private Controller<Product> products;
	private Calendar calendar;

	public Accounting(JStudio app){
		super(app, Invoice.class);
		this.products = new Controller<Product>(app, Product.class);
		this.calendar = Calendar.getInstance();
	}
	
	public Controller<Product> getProducts(){
		return products;
	}
	
	@SuppressWarnings("unchecked")
	public Integer getByNumber(final long id, final long number, final Date date){
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.MONTH, 1);
		String from = dayDateFormat.format(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		String to = dayDateFormat.format(calendar.getTime());
		String sql = "SELECT COUNT(id) FROM "+source+" WHERE id!="+id+" AND number="+number+" AND (date BETWEEN '"+from+"' AND '"+to+"')";
		return (Integer)getApplication().getDatabase().executeQuery(sql);
	}
	
	public Long getNextInvoiceNumber(final Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		Integer y = c.get(Calendar.YEAR);
		String query = "SELECT MAX(number) FROM "+source;
		query += " WHERE date BETWEEN '"+y+"-01-01' AND '"+y+"-12-31'";
		logger.debug("getNextInvoiceNumber: "+query);
		Object o = app.getDatabase().executeQuery(query);
		logger.debug("result is "+o);
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
	
	public void store(final Invoice entity){
		long pid = products.getNextId();
		for(Product p: entity.getProducts()){
			if(p.getId()==0) p.setId(++pid);
			p.setInvoice(entity);
		}
		app.getDatabase().store(source, entity);
	}
	
	public void delete(final Invoice o){
		for(Product p: o.getProducts()){
			products.delete(p);
		}
		app.getDatabase().delete(source, o);
	}
}
