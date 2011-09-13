package jstudio.db;

import java.io.Serializable;

public interface DatabaseObject extends Serializable {

	public Long getId();
	public void setId(Long l);
}
