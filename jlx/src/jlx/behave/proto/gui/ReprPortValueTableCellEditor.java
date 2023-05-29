package jlx.behave.proto.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import jlx.asal.rewrite.*;
import jlx.models.UnifyingBlock.ReprPort;

@SuppressWarnings("serial")
public abstract class ReprPortValueTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	private int rowUnderEdit;
	private ASALSymbolicValue selectedValue;
	
	@Override
	public Object getCellEditorValue() {
		return selectedValue;
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		rowUnderEdit = row;
		selectedValue = (ASALSymbolicValue)value;
		
		JComboBox<ASALSymbolicValue> comboBox = new JComboBox<ASALSymbolicValue>();
		ReprPort p = getPort(row);
		
		for (ASALSymbolicValue v : ASALSymbolicValue.from(p.getReprOwner().getPossibleValues(p))) {
			comboBox.addItem(v);
		}
		
		comboBox.setSelectedItem(selectedValue);
		comboBox.addActionListener(this);
		return comboBox;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent event) {
		JComboBox<ASALSymbolicValue> comboBox = (JComboBox<ASALSymbolicValue>) event.getSource();
		selectedValue = (ASALSymbolicValue)comboBox.getSelectedItem();
		setPort(rowUnderEdit, selectedValue);
	}
	
	public abstract ReprPort getPort(int row);
	public abstract void setPort(int row, ASALSymbolicValue value);
}

