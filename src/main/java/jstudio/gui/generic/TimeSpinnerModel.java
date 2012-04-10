package jstudio.gui.generic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;

import jstudio.model.Event;

public class TimeSpinnerModel extends SpinnerListModel {
	private static final long serialVersionUID = 8373126228193599307L;
	
	public static final int MIN_STEP = 15;
	private static final int MIN_SIZ = 60*24/MIN_STEP;  

	static Calendar calendar = Calendar.getInstance();

	public static Object[] createTimeList(final Date date) {
		ArrayList<Object> list = new ArrayList<Object>(MIN_SIZ);
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		for (int i = 0; i < MIN_SIZ; ++i) {
			calendar.add(Calendar.MINUTE, MIN_STEP);
			list.add(Event.timeFormat.format(calendar.getTime()));
		}
		return list.toArray(new Object[MIN_SIZ]);
	}

	Object firstValue, lastValue;
	SpinnerModel linkedModel = null;

	public TimeSpinnerModel(Date date) {
		super(createTimeList(date));
		firstValue = super.getList().get(0);
		lastValue = super.getList().get(getList().size() - 1);
		calendar.setTime(date);
		if(calendar.get(Calendar.MINUTE)>=30){
			calendar.set(Calendar.MINUTE, 30);
		}else{
			calendar.set(Calendar.MINUTE, 0);
		}
		super.setValue(Event.timeFormat.format(calendar.getTime()));
	}

	public void setLinkedModel(SpinnerModel linkedModel) {
		this.linkedModel = linkedModel;
	}

	public Object getNextValue() {
		Object value = super.getNextValue();
		if (value == null) {
			value = firstValue;
			if (linkedModel != null) {
				linkedModel.setValue(linkedModel.getNextValue());
			}
		}
		return value;
	}

	public Object getPreviousValue() {
		Object value = super.getPreviousValue();
		if (value == null) {
			value = lastValue;
			if (linkedModel != null) {
				linkedModel.setValue(linkedModel.getPreviousValue());
			}
		}
		return value;
	}
}
