/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.controls;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

/**
 * Holds a list of data loaded from database that contains: id, label, enabled.
 *
 * @author El Mhadder Mohamed Rida
 */
public class IdLabelComboModel extends DefaultComboBoxModel<IdLabelComboElement> {
	private static final long serialVersionUID = 5746402049086232397L;

	private final List<IdLabelComboElement> items;

	/**
	 * Init an empty new instance.
	 */
	public IdLabelComboModel() {
		items = new ArrayList<>();
	}

	/**
	 * Init a new instace with a list of items.
	 * @param items List of items to init the model.
	 */
	public IdLabelComboModel(List<IdLabelComboElement> items) {
		this.items = items;
	}

	/**
	 * @return The size of this model.
	 */
	@Override
	public int getSize() { return items.size(); }

	/**
	 * Gets the element at index.
	 * @param index Index of the element to return.
	 * @return Return the element at index.
	 */
	@Override
	public IdLabelComboElement getElementAt(int index) { return items.get(index); }

	/**
	 * Add an element to this model.
	 * @param item Item to add.
	 */
    public void addElement(IdLabelComboElement item) {
        items.add(item);
        int index = items.size() - 1;
        fireIntervalAdded(this, index, index);
    }

    /**
     * Remove an element at index.
     * @param index Index of the element to remove.
     */
    public void removeElement(int index) {
        items.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    /*
     * Added functions
     */

    /**
     * Replace an existant element with a new one.
     * @param old Current element.
     * @param item New element to replace the old one.
     */
    public void replaceElement(IdLabelComboElement old, IdLabelComboElement item) {
		int index = items.indexOf(old);
		items.set(index, item);
		fireContentsChanged(this, index, index);
    }

    /**
     * Remove an element from this model.
     * @param item Element to remove.
     */
    public void removeElement(IdLabelComboElement item) {
		int index = items.indexOf(item);
		if(index >= 0) {
			items.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}
}
