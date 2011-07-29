package jstudio.control;

import jstudio.JStudio;
import jstudio.model.Invoice;
import jstudio.model.Treatment;

public class Accounting extends Controller<Invoice>{
	
	private Controller<Treatment> treatmentManager;

	public Accounting(JStudio app){
		super(app, Invoice.class.getName());
		this.treatmentManager = new Controller<Treatment>(app, Treatment.class.getName());
	}
	
	public Controller<Treatment> getTreatmentManager(){
		return treatmentManager;
	}
}
