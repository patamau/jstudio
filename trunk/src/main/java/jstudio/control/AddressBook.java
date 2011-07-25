package jstudio.control;

import jstudio.db.DatabaseInterface;
import jstudio.model.Person;

public class AddressBook extends Controller<Person>{
	
	public AddressBook(DatabaseInterface database){
		super(database);
		setSource(Person.class.getName());
	}
}
