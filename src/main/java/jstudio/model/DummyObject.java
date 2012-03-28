package jstudio.model;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import jstudio.db.DatabaseObject;

public class DummyObject  implements DatabaseObject {

	private static final long serialVersionUID = -6359419505980691099L;

	@Override
	public Long getId() {
		return 1l;
	}

	@Override
	public void setId(Long l) {
		JOptionPane.showMessageDialog(null, "I AM DUMMY!!!!");
	}

	public Map<String,String> getPrintData(){
		return new HashMap<String,String>();
	}
}
