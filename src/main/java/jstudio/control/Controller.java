package jstudio.control;

import java.util.Collection;

import jstudio.JStudio;
import jstudio.db.DatabaseObject;

/**
 * Agenda holds events on a daily basis.
 * @author Matteo
 *
 */
public abstract class Controller<E extends DatabaseObject> {
	
	private String source;
	private JStudio app;
	
	public Controller(JStudio app, String source){
		this.app = app;
		this.source = source;
	}
	
	public final JStudio getApplication(){
		return app;
	}
	
	public final String getSource(){
		return this.source;
	}
	
	@SuppressWarnings("unchecked")
	public E get(int id){
		return (E)app.getDatabase().get(source, id);
	}
	
	@SuppressWarnings("unchecked")
	public E add(E e){
		return (E)app.getDatabase().store(source, e);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<E> getAll(){
		return (Collection<E>)app.getDatabase().getAll(source);
	}
	
	public void store(E o){
		app.getDatabase().store(source, o);
	}
}
