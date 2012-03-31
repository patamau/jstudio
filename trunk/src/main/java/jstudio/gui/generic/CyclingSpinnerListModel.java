package jstudio.gui.generic;

import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;

public class CyclingSpinnerListModel extends SpinnerListModel {
	private static final long serialVersionUID = -2707353512281848145L;
	private Object firstValue, lastValue;
	private SpinnerModel linkedModel = null;

	public CyclingSpinnerListModel(Object[] values) {
		super(values);
		firstValue = values[0];
		lastValue = values[values.length - 1];
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
		return "cacca";
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
