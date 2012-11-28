package org.isip.states.speech.factories.models;

import java.util.ArrayList;
import java.util.List;

import org.isip.factories.models.UnitAssessment;

public class Speech extends UnitAssessment {

	private List<SpeechItem> items;
	
	public Speech(Integer unitId) {
		super(unitId);
	}

	/**
	 * @return the items
	 */
	public List<SpeechItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<SpeechItem> items) {
		this.items = items;
	}
	
	public void addItem(SpeechItem i) {
		if (items == null) {
			items = new ArrayList<SpeechItem>();
		}
		items.add(i);
	}

}
