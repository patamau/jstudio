package jstudio.control;

import jstudio.JStudio;
import jstudio.model.Invoice;
import jstudio.model.Product;

public class Accounting extends Controller<Invoice>{
	
	private Controller<Product> products;

	public Accounting(JStudio app){
		super(app, Invoice.class.getName());
		this.products = new Controller<Product>(app, Product.class.getName());
	}
	
	public Controller<Product> getProducts(){
		return products;
	}
}
