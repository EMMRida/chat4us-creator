/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.controls;

/**
 * Holds items that are loaded from database that has fields: id, label, enabled.
 *
 * @author El Mhadder Mohamed Rida
 */
public class IdLabelComboElement {
    private final int id;
    private final String label;
    private boolean enabled;

    /**
     * Init an IdLabelComboElement instance
     * @param id Id of the element on database
     * @param label Label/Text of the element.
     * @param enabled Element state.
     */
    public IdLabelComboElement(int id, String label, boolean enabled) {
        this.id = id;
        this.label = label;
        this.enabled = enabled;
    }

    /**
     * @return Id of the element.
     */
    public int getId() { return id; }

    /**
     * @return Label of the element
     */
    public String getLabel() { return label; }

    /**
     * @return Element state.
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Sets the element state.
     * @param enabled New element state.
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * @return Element label.
     */
    @Override public String toString() { return this.label; }
}
