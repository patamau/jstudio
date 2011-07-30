package jstudio.control;

import jstudio.JStudio;
import jstudio.model.Invoice;
import jstudio.model.Product;

public class Accounting extends Controller<Invoice>{
	
	private Controller<Product> treatmentManager;

	public Accounting(JStudio app){
		super(app, Invoice.class.getName());
		this.treatmentManager = new Controller<Product>(app, Product.class.getName());
	}
	
	public Controller<Product> getTreatmentManager(){
		return treatmentManager;
	}
}
