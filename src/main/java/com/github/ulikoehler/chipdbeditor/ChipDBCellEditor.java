/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ulikoehler.chipdbeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

/**
 * This cell editor is needed to convert the JLabelWithAttribute objects to
 * and from the ChipDB and rendered representation
 * @author uli
 */
public class ChipDBCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JLabelWithAttribute currentValue = null;

    public ChipDBCellEditor() {
    }

    public Object getCellEditorValue() {
        return currentValue;
    }

    public Component getTableCellEditorComponent(JTable table, Object cellValue, boolean isSelected, int row, int column) {
        //Check if the last field content has not yet been inserted into the component
        if (cellValue instanceof String) {
            currentValue = new JLabelWithAttribute(cellValue.toString(), cellValue);
        } else {
            this.currentValue = ((JLabelWithAttribute) cellValue);
        }
        if (currentValue == null) {
            currentValue = new JLabelWithAttribute("", "");
        }
        String originalString = currentValue.getAttribute().toString();
        final JTextField field = new JTextField(originalString);
        field.setBorder(new LineBorder(Color.BLACK));
        field.addVetoableChangeListener(new VetoableChangeListener() {

            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                System.out.println("VC!!");
            }
        });
        field.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                currentValue.setAttribute(field.getText());
                currentValue.setText(ChipDBSyntax.chipDBSyntaxToHTML(field.getText()));
            }

            public void removeUpdate(DocumentEvent e) {
                currentValue.setAttribute(field.getText());
                currentValue.setText(ChipDBSyntax.chipDBSyntaxToHTML(field.getText()));
            }

            public void changedUpdate(DocumentEvent e) {
                currentValue.setAttribute(field.getText());
                currentValue.setText(ChipDBSyntax.chipDBSyntaxToHTML(field.getText()));
            }
        });
        return field;
    }
}
