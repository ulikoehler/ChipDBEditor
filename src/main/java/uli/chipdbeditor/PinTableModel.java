/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uli.chipdbeditor;

import java.util.HashMap;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author uli
 */
class PinTableModel extends AbstractTableModel {
    private int numPins = 8;
    private Map<Integer, String> pinToSymbol = new HashMap<Integer, String>();
    private Map<Integer, String> pinToDescription = new HashMap<Integer, String>();

    public Map<Integer, String> getPinToDescription() {
        return pinToDescription;
    }

    public Map<Integer, String> getPinToSymbol() {
        return pinToSymbol;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex == 0) {
            return String.class;
        } else {
            return JLabelWithAttribute.class;
        }
    }
    
    

    public void setNumPins(int newNumPins) {
        int oldNumPins = this.numPins;
        this.numPins = newNumPins;
        if (oldNumPins > newNumPins) {
            fireTableRowsDeleted(newNumPins, oldNumPins - 1); //zero-based!
        } else {
            fireTableRowsInserted(oldNumPins, newNumPins - 1);
        }
    }

    public int getRowCount() {
        return numPins;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Pin";
        } else if (column == 1) {
            return "Symbol";
        } else if (column == 2) {
            return "Description";
        } else {
            throw new IllegalArgumentException("Illegal column nr: " + column);
        }
    }

    public int getColumnCount() {
        return 3;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !(columnIndex == 0); //Pin number is NOT editable, everything else is
    }


    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return "" + (rowIndex + 1); //Pin number
        } else if (columnIndex == 1) {
            //Symbol
            return pinToSymbol.get(rowIndex + 1);
        } else if (columnIndex == 2) {
            //Description
            return pinToDescription.get(rowIndex + 1);
        } else {
            throw new IllegalArgumentException("Illegal column nr: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String attribute = null;
        if(aValue instanceof JLabelWithAttribute) {
            attribute = ((JLabelWithAttribute) aValue).getAttribute().toString();
        } else {
            attribute = aValue.toString();
        }
        if (columnIndex == 0) {
            throw new IllegalArgumentException("Pin number is not editable!");
        } else if (columnIndex == 1) {
            //Symbol
            pinToSymbol.put(rowIndex + 1, attribute);
        } else if (columnIndex == 2) {
            //Description
            pinToDescription.put(rowIndex + 1, attribute);
        } else {
            throw new IllegalArgumentException("Illegal column nr: " + columnIndex);
        }
        fireTableDataChanged();
    }
    
}
