/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.controls;

import java.util.List;

import javax.swing.AbstractListModel;

import io.github.emmrida.chat4us.core.ChatServer;

/**
 * ChatServer objects list model.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatServerListModel extends AbstractListModel<ChatServer> {
	private static final long serialVersionUID = -888655735971242341L;

	private final List<ChatServer> items;

	/**
	 * Init ChatServer list model.
	 * @param items Items to add to this model.
	 */
	public ChatServerListModel(List<ChatServer> items) {
		this.items = items;
	}

	/**
	 * Returns the number of elements in this list.
	 */
	@Override
	public int getSize() { return items.size(); }

	/**
	 * Returns the element at the specified index in this list.
	 * @param index Index of the element to return.
	 * @return The element at the specified index in this list.
	 */
	@Override
	public ChatServer getElementAt(int index) { return items.get(index); }

	/**
	 * Add a ChatServer element
	 * @param item A ChatServer object.
	 */
    public void addElement(ChatServer item) {
        items.add(item);
        int index = items.size() - 1;
        fireIntervalAdded(this, index, index);
    }

    /**
     * Removes an element from this model
     * @param index  Index of the element to remove.
     */
    public void removeElement(int index) {
        items.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    /*
     * Added functions
     */

    /**
     * Replaces a ChatServer object with a new one.
     * @param old Current item on this model.
     * @param item New item to replace the old one.
     */
    public void replaceElement(ChatServer old, ChatServer item) {
		int index = items.indexOf(old);
		items.set(index, item);
		fireContentsChanged(this, index, index);
    }

    /**
     * Remove a ChatServer object from this model.
     * @param item ChatServer object to remove.
     */
    public void removeElement(ChatServer item) {
		int index = items.indexOf(item);
		if(index >= 0) {
			items.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}
}
