package jstudio.model;

import javax.swing.JOptionPane;

import jstudio.db.DatabaseObject;

public class DummyObject  implements DatabaseObject {

	@Override
	public Long getId() {
		return 1l;
	}

	@Override
	public void setId(Long l) {
		JOptionPane.showMessageDialog(null, "I AM DUMMY!!!!");
	}

}
