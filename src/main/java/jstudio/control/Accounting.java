package jstudio.control;

import jstudio.JStudio;
import jstudio.model.Invoice;

public class Accounting extends Controller<Invoice>{

	public Accounting(JStudio app){
		super(app, Invoice.class.getName());
	}
}
