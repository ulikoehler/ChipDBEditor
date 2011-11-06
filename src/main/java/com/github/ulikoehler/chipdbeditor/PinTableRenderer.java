/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ulikoehler.chipdbeditor;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author uli
 */
public class PinTableRenderer implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return new JLabelWithAttribute("", "");
        }

        if (column == 0) { //Pin number
            return new JLabelWithAttribute(value.toString(), value.toString());
        } else {
            return new JLabelWithAttribute(ChipDBSyntax.chipDBSyntaxToHTML(value.toString()), value.toString());
        }
    }
}
