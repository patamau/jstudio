package jstudio.control;

import jstudio.db.DatabaseInterface;
import jstudio.model.Invoice;

public class Accounting extends Controller<Invoice>{

	public Accounting(DatabaseInterface database){
		super(database);
		setSource(Invoice.class.getName());
	}
}
