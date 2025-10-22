/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.controls;

import java.util.List;
import javax.swing.AbstractListModel;

import io.github.emmrida.chat4usagent.core.ChatSession;

/**
 * Chat session list model.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatSessionListModel extends AbstractListModel<ChatSession> {
	private static final long serialVersionUID = 8052370988181400152L;

	private final List<ChatSession> items;

	/** @see AbstractListModel#AbstractListModel() */
	public ChatSessionListModel(List<ChatSession> items) {
		this.items = items;
	}

	/** @see javax.swing.ListModel#getSize() */
	@Override
	public int getSize() { return items.size(); }

	/** @see javax.swing.ListModel#getElementAt(int) */
	@Override
	public ChatSession getElementAt(int index) { return items.get(index); }

	/**
	 * Add an element to this model.
	 * @param item Item to add.
	 */
   public void addElement(ChatSession item) {
       items.add(item);
       int index = items.size() - 1;
       fireIntervalAdded(this, index, index);
   }

   /**
    * Remove an element from this model.
    * @param index Index of the element to remove.
    */
   public void removeElement(int index) {
       items.remove(index);
       fireIntervalRemoved(this, index, index);
   }

   /**
    * Remove an element from this model.
    * @param item Element to remove.
    */
   public void removeElement(ChatSession item) {
		int index = items.indexOf(item);
		if(index >= 0) {
			items.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}

   /**
	* Check if this model contains an element.
	* @param item Element to check.
	* @return True if this model contains the element.
	*/
   public boolean contains(ChatSession item) { return items.contains(item); }
}











