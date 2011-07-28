package jstudio.control;

import java.util.HashMap;
import java.util.List;

import jstudio.JStudio;
import jstudio.model.Comune;

public class Comuni extends Controller<Comune>{
	
	public Comuni(JStudio app){
		super(app, Comune.class.getName());
	}
	
	@SuppressWarnings("unchecked")
	public String getCode(String pv, String cm){
		HashMap<String, String> map = new HashMap<String,String>();
		map.put("provincia", pv);
		map.put("comune",cm);
		List<Comune> list = (List<Comune>) getApplication().getDatabase().getAll(getSource(), map);
		if(list.size()==0) return null;
		else return list.get(0).getIdNazionale();
	}
}
