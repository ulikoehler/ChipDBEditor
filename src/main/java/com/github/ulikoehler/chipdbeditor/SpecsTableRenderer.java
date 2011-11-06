/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ulikoehler.chipdbeditor;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author uli
 */
public class SpecsTableRenderer implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(value == null) {
            return new JLabelWithAttribute("","");
        }
        return new JLabelWithAttribute(ChipDBSyntax.chipDBSyntaxToHTML(value.toString()), value.toString());
    }
}
