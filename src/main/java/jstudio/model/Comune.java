package jstudio.model;

import jstudio.db.DatabaseObject;


public class Comune implements DatabaseObject, Comparable<Object> {

	private static final long serialVersionUID = 8448156929661777007L;
	
	private Long id;
	private String idNazionale, idCatastale, provincia, comune;
	
	public Comune(){
		
	}

	public Long getId(){
		return id;
	}
	
	public String getIdNazionale() {
		return idNazionale;
	}

	public void setIdNazionale(String id_nazionale) {
		this.idNazionale = id_nazionale;
	}

	public String getIdCatastale() {
		return idCatastale;
	}

	public void setIdCatastale(String idCatastale) {
		this.idCatastale = idCatastale;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	public String getComune() {
		return comune;
	}

	public void setComune(String comune) {
		this.comune = comune;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean equals(Object o){
		if(o instanceof Comune){
			Comune p = (Comune)o;
			return p.id==id;
		}
		return false;
	}
	
	public int compareTo(Object o){
		if(o instanceof Comune){
			Comune p = (Comune)o;
			if(p.id==id) return 0;
			if(p.id<id) return -1;
		}
		return 1;
	}
}
