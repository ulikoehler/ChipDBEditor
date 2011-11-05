/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uli.chipdbeditor;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *
 * @author uli
 */
public class JLabelWithAttribute extends JLabel {

    public JLabelWithAttribute(Icon image) {
        super(image);
    }

    public JLabelWithAttribute(String text) {
        super(text);
    }

    public JLabelWithAttribute(String text, Object attribute) {
        super(text);
        this.attribute = attribute;
    }

    public JLabelWithAttribute(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    public JLabelWithAttribute() {
    }

    public JLabelWithAttribute(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
    }

    public JLabelWithAttribute(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
    }
    private Object attribute = null;

    public Object getAttribute() {
        return attribute;
    }

    public void setAttribute(Object attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        return "JLabelWithAttribute{" + "attribute=" + attribute + '}';
    }
}
