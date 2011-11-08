/*
 * ChipDBEditorFrame.java
 *
 * Created on 04.11.2011, 20:36:04
 */
package com.github.ulikoehler.chipdbeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author uli
 */
public class ChipDBEditorFrame extends javax.swing.JFrame {

    private PinTableModel pinTableModel = new PinTableModel();
    private SpecsTableModel specsTableModel = new SpecsTableModel();
    private NotesTableModel notesTableModel = new NotesTableModel();
    private boolean disableYAMLParsing = false; //Avoid parsing the YAML because of setting the field content when recalculating it
    private boolean disableYAMLRebuild = false; //Avoid rebuilding the YAML when parsing it

    private void initPinsTable() {
        pinsTable.setModel(pinTableModel);
        pinsTable.setRowHeight(25);
        pinsTable.setDefaultRenderer(JLabelWithAttribute.class, new PinTableRenderer());
        pinsTable.setDefaultEditor(JLabelWithAttribute.class, new ChipDBCellEditor());
        //The pin column doesn't have to be THAT wide!
        pinsTable.getColumnModel().getColumn(0).setMinWidth(40);
        pinsTable.getColumnModel().getColumn(0).setMaxWidth(40);
    }

    private void initSpecsTable() {
        specsTable.setModel(specsTableModel);
        specsTable.setRowHeight(25);
        specsTable.setDefaultRenderer(JLabelWithAttribute.class, new SpecsTableRenderer());
        specsTable.setDefaultEditor(JLabelWithAttribute.class, new ChipDBCellEditor());
    }

    private void initNotesTable() {
        notesTable.setModel(notesTableModel);
        notesTable.setRowHeight(25);
        notesTable.setDefaultRenderer(JLabelWithAttribute.class, new SpecsTableRenderer()); //The spec renderer can be used for this one, too
        notesTable.setDefaultEditor(JLabelWithAttribute.class, new ChipDBCellEditor());
    }

    private void initListeners() {
        TableModelListener tableListener = new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                rebuildYAML();
            }
        };
        DocumentListener docListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                rebuildYAML();
            }

            public void removeUpdate(DocumentEvent e) {
                rebuildYAML();
            }

            public void changedUpdate(DocumentEvent e) {
                rebuildYAML();
            }
        };
        pinsTable.getModel().addTableModelListener(tableListener);
        specsTable.getModel().addTableModelListener(tableListener);
        notesTable.getModel().addTableModelListener(tableListener);
        partField.getDocument().addDocumentListener(docListener);
        familyField.getDocument().addDocumentListener(docListener);
        datasheetField.getDocument().addDocumentListener(docListener);
        descriptionField.getDocument().addDocumentListener(docListener);
        aliasesField.getDocument().addDocumentListener(docListener);
        packageComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rebuildYAML();
            }
        });
        //Parse the YAML when it changed in the field
        yamlField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                try {
                    parseYAML(yamlField.getText());
                } catch (IOException ex) {
                    Logger.getLogger(ChipDBEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                try {
                    parseYAML(yamlField.getText());
                } catch (IOException ex) {
                    Logger.getLogger(ChipDBEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            public void changedUpdate(DocumentEvent e) {
                try {
                    parseYAML(yamlField.getText());
                } catch (IOException ex) {
                    Logger.getLogger(ChipDBEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void rebuildYAML() {
        if (disableYAMLRebuild) {
            return;
        }
        StringBuilder yamlBuilder = new StringBuilder();
        yamlBuilder.append(headerToYAML());
        yamlBuilder.append(pinsToYAML());
        yamlBuilder.append(specsToYAML());
        yamlBuilder.append(notesToYAML());
        disableYAMLParsing = true;
        yamlField.setText(yamlBuilder.toString());
        disableYAMLParsing = false;
    }

    private String headerToYAML() {
        StringBuilder yamlBuilder = new StringBuilder("---\n");
        yamlBuilder.append("name: ").append(partField.getText()).append('\n');
        yamlBuilder.append("description: \"").append(descriptionField.getText()).append("\"\n");
        yamlBuilder.append("aliases: [").append(aliasesField.getText()).append("]\n");
        yamlBuilder.append("package: ").append(packageComboBox.getSelectedItem().toString()).append('\n');
        yamlBuilder.append("pincount: ").append(pinsSpinner.getIntValue()).append('\n');
        yamlBuilder.append("family: \"").append(familyField.getText()).append("\"\n");
        yamlBuilder.append("datasheet: \"").append(datasheetField.getText()).append("\"\n");
        return yamlBuilder.toString();
    }

    private String notesToYAML() {
        StringBuilder yamlBuilder = new StringBuilder("notes:\n");
        for (Map.Entry<Integer, String> entry : notesTableModel.getNotes().entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                continue; //Don't add empty notes
            }
            yamlBuilder.append("  - \"").append(entry.getValue()).append("\"\n");
        }
        return yamlBuilder.toString();
    }

    private String specsToYAML() {
        StringBuilder yamlBuilder = new StringBuilder("specs:\n");
        int numPins = pinsSpinner.getIntValue();
        Map<Integer, String> paramById = specsTableModel.getParameterById();
        Map<Integer, String> unitById = specsTableModel.getUnitById();
        Map<Integer, String> valueById = specsTableModel.getValueById();
        for (int i = 1; i <= numPins; i++) {
            String param = paramById.get(i) == null ? "" : paramById.get(i);
            String unit = unitById.get(i) == null ? "" : unitById.get(i);
            String value = valueById.get(i) == null ? "" : valueById.get(i);
            if (param.trim().isEmpty() && unit.trim().isEmpty() && value.trim().isEmpty()) {
                System.out.println("Continue ");
                continue; //Don't add empty specs
            }
            yamlBuilder.append("  - param: \"").append(param).append("\"\n");
            yamlBuilder.append("    val: ").append(value).append("\n");
            yamlBuilder.append("    unit: \"").append(unit).append("\"\n");
        }
        return yamlBuilder.toString();
    }

    private void parseYAML(String yaml) throws IOException {
        if (disableYAMLParsing) {
            return;
        }
        //Remove any old data
        notesTableModel.clear();
        specsTableModel.clear();
        disableYAMLRebuild = true;
        BufferedReader in = new BufferedReader(new StringReader(yaml));
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("name:")) {
                String name = line.substring("name:".length()).trim();
                if (name.startsWith("\"") && name.endsWith("\"")) {
                    name = name.substring(1, name.length() - 1);
                }
                partField.setText(name);
            } else if (line.startsWith("description:")) {
                String description = line.substring("description:".length()).trim();
                if (description.startsWith("\"") && description.endsWith("\"")) {
                    description = description.substring(1, description.length() - 1);
                }
                descriptionField.setText(description);
            } else if (line.startsWith("family:")) {
                String family = line.substring("family:".length()).trim();
                if (family.startsWith("\"") && family.endsWith("\"")) {
                    family = family.substring(1, family.length() - 1);
                }
                familyField.setText(family);
            } else if (line.startsWith("package:")) {
                String packageString = line.substring("package:".length()).trim();
                if (packageString.startsWith("\"") && packageString.endsWith("\"")) {
                    packageString = packageString.substring(1, packageString.length() - 1);
                }
                packageComboBox.setSelectedItem(packageString);
            } else if (line.startsWith("datasheet:")) {
                String datasheet = line.substring("datasheet:".length()).trim();
                if (datasheet.startsWith("\"") && datasheet.endsWith("\"")) {
                    datasheet = datasheet.substring(1, datasheet.length() - 1);
                }
                datasheetField.setText(datasheet);
            } else if (line.startsWith("aliases:")) {
                String aliases = line.substring("aliases:".length()).trim();
                if (aliases.startsWith("[") && aliases.endsWith("]")) {
                    aliases = aliases.substring(1, aliases.length() - 1);
                }
                aliasesField.setText(aliases);
            } else if (line.startsWith("pincount:")) {
                String pincountString = line.substring("pincount:".length()).trim();
                int pincountInt = Integer.parseInt(pincountString);
                pinsSpinner.setIntValue(pincountInt);
            } else if (line.trim().startsWith("- num:")) {
                String pincountString = line.trim().substring("- num:".length()).trim();
                int currentPinCount = Integer.parseInt(pincountString);
                //Parse the symbol
                do {
                    line = in.readLine().trim();
                } while (line.isEmpty());
                //Check if the next line contains the symbol
                if (line == null || !line.startsWith("sym:")) {
                    JOptionPane.showMessageDialog(this, "Error while parsing sym line: \"" + line + "\"", "Parsing error", JOptionPane.ERROR_MESSAGE);
                }
                line = line.substring("sym:".length()).trim();
                if (line.startsWith("\"") && line.endsWith("\"")) {
                    line = line.substring(1, line.length() - 1);
                }
                String symbol = line;
                //Parse the description
                do {
                    line = in.readLine().trim();
                } while (line.isEmpty());
                //Check if the next line contains the description
                if (line == null || !line.startsWith("desc:")) {
                    JOptionPane.showMessageDialog(this, "Error while parsing description line: " + line, "Parsing error", JOptionPane.ERROR_MESSAGE);
                }
                line = line.substring("desc:".length()).trim();
                if (line.startsWith("\"") && line.endsWith("\"")) {
                    line = line.substring(1, line.length() - 1);
                }
                String description = line;
                //Insert the data and re-render the table row
                pinTableModel.addPinData(currentPinCount, symbol, description);
            } else if (line.trim().startsWith("- param:")) {
                line = line.trim().substring("- param:".length()).trim();
                if (line.startsWith("\"") && line.endsWith("\"")) {
                    line = line.substring(1, line.length() - 1);
                }
                String param = line;
                System.out.println(param);
                //Parse the symbol
                do {
                    line = in.readLine().trim();
                } while (line == null || line.isEmpty());
                if (line == null || !line.startsWith("val:")) {
                    JOptionPane.showMessageDialog(this, "Error while parsing value line: " + line, "Parsing error", JOptionPane.ERROR_MESSAGE);
                }
                line = line.substring("val:".length()).trim();
                if (line.startsWith("\"") && line.endsWith("\"")) {
                    line = line.substring(1, line.length() - 1);
                }
                String value = line;
                //Parse the description
                do {
                    line = in.readLine().trim();
                } while (line.isEmpty());
                if (line == null || !line.startsWith("unit:")) {
                    JOptionPane.showMessageDialog(this, "Error while parsing unit line: " + line, "Parsing error", JOptionPane.ERROR_MESSAGE);
                }
                line = line.substring("unit:".length()).trim();
                if (line.startsWith("\"") && line.endsWith("\"")) {
                    line = line.substring(1, line.length() - 1);
                }
                String unit = line;
                //Insert the specification
                specsTableModel.addSpecification(param, value, unit);
            } else if (line.startsWith("notes:")) {
                while ((line = in.readLine()) != null) {
                    if (!line.trim().startsWith("-")) {
                        break; //Stop parsing notes
                    }
                    line = line.trim().substring("-".length()).trim();
                    if (line.startsWith("\"") && line.endsWith("\"")) {
                        line = line.substring(1, line.length() - 1);
                    }
                    notesTableModel.addNote(line);
                }
            }
        }
        disableYAMLRebuild = false;
    }

    private String pinsToYAML() {
        StringBuilder yamlBuilder = new StringBuilder("pins:\n");
        int numPins = pinsSpinner.getIntValue();
        Map<Integer, String> pinToSymbol = pinTableModel.getPinToSymbol();
        Map<Integer, String> pinToDescription = pinTableModel.getPinToDescription();
        for (int i = 1; i <= numPins; i++) {
            String symbol = pinToSymbol.get(i);
            String description = pinToDescription.get(i);
            yamlBuilder.append("  - num: ").append(i).append('\n');
            yamlBuilder.append("    sym: ").append(symbol == null ? "" : symbol).append('\n');
            yamlBuilder.append("    desc: ").append(description == null ? "" : description).append('\n');
        }
        return yamlBuilder.toString();
    }

    /** Creates new form ChipDBEditorFrame */
    public ChipDBEditorFrame() {
        setLocationRelativeTo(null);
        initComponents();
        initPinsTable();
        initSpecsTable();
        initNotesTable();
        initListeners();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pinsPanel = new javax.swing.JPanel();
        pinsScrollPane = new javax.swing.JScrollPane();
        pinsTable = new javax.swing.JTable();
        pinsLabel = new javax.swing.JLabel();
        pinsSpinner = new com.github.ulikoehler.chipdbeditor.NumberSpinner();
        specsPanel = new javax.swing.JPanel();
        specsScrollPane = new javax.swing.JScrollPane();
        specsTable = new javax.swing.JTable();
        notesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notesTable = new javax.swing.JTable();
        yamlScrollPane = new javax.swing.JScrollPane();
        yamlField = new javax.swing.JTextArea();
        partLabel = new javax.swing.JLabel();
        partField = new javax.swing.JTextField();
        familyLabel = new javax.swing.JLabel();
        familyField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        datasheetField = new javax.swing.JTextField();
        datasheetLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextField();
        aliasesLabel = new javax.swing.JLabel();
        aliasesField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ChipDB editor");

        pinsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pins"));

        pinsTable.setAutoCreateRowSorter(true);
        pinsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        pinsTable.setCellSelectionEnabled(true);
        pinsScrollPane.setViewportView(pinsTable);

        pinsLabel.setText("Number of pins:");

        pinsSpinner.setIntValue(8);
        pinsSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pinsSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pinsPanelLayout = new javax.swing.GroupLayout(pinsPanel);
        pinsPanel.setLayout(pinsPanelLayout);
        pinsPanelLayout.setHorizontalGroup(
            pinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pinsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pinsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                    .addGroup(pinsPanelLayout.createSequentialGroup()
                        .addComponent(pinsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pinsSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pinsPanelLayout.setVerticalGroup(
            pinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pinsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pinsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pinsLabel)
                    .addComponent(pinsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        specsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Specifications"));

        specsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        specsScrollPane.setViewportView(specsTable);

        javax.swing.GroupLayout specsPanelLayout = new javax.swing.GroupLayout(specsPanel);
        specsPanel.setLayout(specsPanelLayout);
        specsPanelLayout.setHorizontalGroup(
            specsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(specsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(specsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                .addContainerGap())
        );
        specsPanelLayout.setVerticalGroup(
            specsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(specsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(specsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );

        notesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Notes"));

        notesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(notesTable);

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 407, Short.MAX_VALUE)
            .addGroup(notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(notesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
            .addGroup(notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(notesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        yamlField.setColumns(20);
        yamlField.setRows(5);
        yamlScrollPane.setViewportView(yamlField);

        partLabel.setText("Part:");

        familyLabel.setText("Family:");

        jLabel1.setText("Package:");

        packageComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DIP", "SOIC" }));

        datasheetLabel.setText("Datasheet:");

        descriptionLabel.setText("Description:");

        aliasesLabel.setText("Aliases (comma-separated):");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(specsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pinsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yamlScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                            .addComponent(notesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(partLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(partField, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(familyLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(familyField, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datasheetLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datasheetField, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(descriptionField, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(aliasesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aliasesField, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(partLabel)
                    .addComponent(partField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(familyLabel)
                    .addComponent(familyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasheetLabel)
                    .addComponent(datasheetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descriptionLabel)
                    .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aliasesLabel)
                    .addComponent(aliasesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(notesPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pinsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yamlScrollPane)
                    .addComponent(specsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pinsSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pinsSpinnerStateChanged
        pinTableModel.setNumPins(pinsSpinner.getIntValue());
    }//GEN-LAST:event_pinsSpinnerStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChipDBEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChipDBEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChipDBEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChipDBEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    new ChipDBEditorFrame().setVisible(true);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ChipDBEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(ChipDBEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ChipDBEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(ChipDBEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField aliasesField;
    private javax.swing.JLabel aliasesLabel;
    private javax.swing.JTextField datasheetField;
    private javax.swing.JLabel datasheetLabel;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField familyField;
    private javax.swing.JLabel familyLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JTable notesTable;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JTextField partField;
    private javax.swing.JLabel partLabel;
    private javax.swing.JLabel pinsLabel;
    private javax.swing.JPanel pinsPanel;
    private javax.swing.JScrollPane pinsScrollPane;
    private com.github.ulikoehler.chipdbeditor.NumberSpinner pinsSpinner;
    private javax.swing.JTable pinsTable;
    private javax.swing.JPanel specsPanel;
    private javax.swing.JScrollPane specsScrollPane;
    private javax.swing.JTable specsTable;
    private javax.swing.JTextArea yamlField;
    private javax.swing.JScrollPane yamlScrollPane;
    // End of variables declaration//GEN-END:variables
}
