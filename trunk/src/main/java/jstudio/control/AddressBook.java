package jstudio.control;

import jstudio.JStudio;
import jstudio.model.Person;

public class AddressBook extends Controller<Person>{
	
	public AddressBook(JStudio app){
		super(app,Person.class);
	}
}
