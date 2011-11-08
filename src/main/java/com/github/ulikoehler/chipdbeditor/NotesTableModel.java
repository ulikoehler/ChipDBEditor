/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ulikoehler.chipdbeditor;

import java.util.HashMap;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author uli
 */
public class NotesTableModel extends AbstractTableModel {

    private Map<Integer, String> notes = new HashMap<Integer, String>(); //First value: key = 0
    
    public void clear() {
        notes.clear();
    }
    
    public void addNote(String note) {
        int index = getRowCount() - 1;
        System.out.println("inde" + index);
        notes.put(index, note);
        fireTableRowsInserted(index-1, index-1);
    }


    public Map<Integer, String> getNotes() {
        return notes;
    }

    public int getRowCount() {
        return notes.size() + 1;
    }

    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Note";
        } else {
            throw new IllegalArgumentException("Illegal column nr: " + column);
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return notes.get(rowIndex);
        } else {
            throw new IllegalArgumentException("Illegal column nr: " + columnIndex);
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return JLabelWithAttribute.class;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        boolean addNewRow = false;
        //Check if we need to add a new row;
        if (rowIndex == getRowCount() - 1) {
            if (!((JLabelWithAttribute) aValue).getAttribute().toString().trim().isEmpty()) { //If there is any content...
                addNewRow = true;
            }
        }
        
        String attribute = null;
        if(aValue instanceof JLabelWithAttribute) {
            attribute = ((JLabelWithAttribute) aValue).getAttribute().toString();
        } else {
            attribute = aValue.toString();
        }

        if (columnIndex == 0) {
            notes.put(rowIndex, attribute);
        } else {
            throw new IllegalArgumentException("Illegal column nr: " + columnIndex);
        }
        
        fireTableDataChanged();

        //Fire the row add event if neccessary
        if (addNewRow) {
            fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
}
