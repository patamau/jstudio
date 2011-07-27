package jstudio.control;

import java.util.HashMap;
import java.util.List;

import jstudio.db.DatabaseInterface;
import jstudio.model.Comune;

public class Comuni extends Controller<Comune>{
	
	public Comuni(DatabaseInterface database){
		super(database);
		setSource(Comune.class.getName());
	}
	
	public String getCode(String pv, String cm){
		HashMap<String, String> map = new HashMap<String,String>();
		map.put("provincia", pv);
		map.put("comune",cm);
		List<Comune> list = (List<Comune>) database.getAll(getSource(), map);
		if(list.size()==0) return null;
		else return list.get(0).getIdNazionale();
	}
}
