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
public class SpecsTableModel extends AbstractTableModel {

    private Map<Integer, String> parameterById = new HashMap<Integer, String>();
    private Map<Integer, String> valueById = new HashMap<Integer, String>();
    private Map<Integer, String> unitById = new HashMap<Integer, String>();
    
    public void clear() {
        parameterById.clear();
        valueById.clear();
        unitById.clear();
    }

    public void addSpecification(String parameter, String value, String unit) {
        int index = getRowCount() - 1;
        parameterById.put(index, parameter);
        valueById.put(index, value);
        unitById.put(index, unit);
        fireTableRowsInserted(index, index);
    }

    public Map<Integer, String> getParameterById() {
        return parameterById;
    }

    public Map<Integer, String> getUnitById() {
        return unitById;
    }

    public Map<Integer, String> getValueById() {
        return valueById;
    }

    public int getRowCount() {
        return Math.max(Math.max(parameterById.size(), valueById.size()), unitById.size()) + 1;
    }

    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Parameter";
        } else if (column == 1) {
            return "Value";
        } else if (column == 2) {
            return "Unit";
        } else {
            throw new IllegalArgumentException("Illegal column nr: " + column);
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return parameterById.get(rowIndex);
        } else if (columnIndex == 1) {
            //Symbol
            return valueById.get(rowIndex);
        } else if (columnIndex == 2) {
            //Description
            return unitById.get(rowIndex);
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
        if (aValue instanceof JLabelWithAttribute) {
            attribute = ((JLabelWithAttribute) aValue).getAttribute().toString();
        } else {
            attribute = aValue.toString();
        }

        if (columnIndex == 0) {
            parameterById.put(rowIndex, attribute);
        } else if (columnIndex == 1) {
            valueById.put(rowIndex, attribute);
        } else if (columnIndex == 2) {
            unitById.put(rowIndex, attribute);
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
