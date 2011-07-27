package jstudio.control;

import jstudio.db.DatabaseInterface;
import jstudio.model.Comune;

public class Comuni extends Controller<Comune>{
	
	public Comuni(DatabaseInterface database){
		super(database);
		setSource(Comune.class.getName());
	}
}
