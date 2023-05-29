package jlx.behave.proto.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import jlx.asal.j.JScope;
import jlx.behave.proto.*;

@SuppressWarnings("serial")
public class DecaFourHistoryGUI extends JFrame {
	private final DecaFourExplorerGUI owner;
	private final DefaultTableModel model;
	private final JTable table;
	private final JButton exploreButton;
	private final JButton coverageButton;
	
	private int selectionRow;
	
	public DecaFourHistoryGUI(DecaFourExplorerGUI owner) {
		super("DecaFourStateMachines History");
		
		this.owner = owner;
		
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		
		model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		model.addColumn("State");
		
		for (JScope scope : owner.getOrderedScopes()) {
			model.addColumn(scope.getName());
		}
		
		model.addRow(createTableRow(1, owner.getInitConfig()));
		selectionRow = 0;
		
		table = new JTable(model);
		exploreButton = new JButton("View in explorer");
		coverageButton = new JButton("Compute coverage");
		
		JPanel buttonPanel = new JPanel();
		initButtonPanel(buttonPanel);
		
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.weightx = 1;
		c1.weighty = 0;
		c1.gridx = 0;
		c1.gridy = 0;
		main.add(buttonPanel, c1);
		
		JPanel tablePanel = new JPanel();
		initTablePanel(tablePanel);
		
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridwidth = 1;
		c2.gridheight = 1;
		c2.fill = GridBagConstraints.BOTH;
		c2.weightx = 1;
		c2.weighty = 1;
		c2.gridx = 0;
		c2.gridy = 1;
		main.add(tablePanel, c2);
		
		setContentPane(main);
		
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	private Object[] createTableRow(int t, DecaFourStateConfig config) {
		Object[] result = new Object[1 + owner.getOrderedScopes().size()];
		result[0] = t;
		
		for (int i = 0; i < owner.getOrderedScopes().size(); i++) {
			JScope scope = owner.getOrderedScopes().get(i);
			result[i + 1] = config.getVtxs().get(scope);
		}
		
		return result;
	}
	
	private void initTablePanel(JPanel p) {
		p.setLayout(new BorderLayout());
		
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//table.setAutoCreateRowSorter(true);
		p.add(table, BorderLayout.CENTER);
		
		for (int i = 0; i <  table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(tableRenderer);
		}
		
		JScrollPane scrollPane = new JScrollPane(table);
		p.add(scrollPane, BorderLayout.CENTER);
	}
	
	private void initButtonPanel(JPanel p) {
		exploreButton.addActionListener(e -> {
			if (table.getSelectedRowCount() == 1) {
				selectionRow = table.getSelectedRow();
				owner.updateDisplay();
				owner.requestFocus();
			}
		});
		p.add(exploreButton);
		coverageButton.addActionListener(e -> {
			owner.getStateMachines().initCfgs();
			DecaFourCoverage3 cov = new DecaFourCoverage3(getConfigList(), owner.getStateMachines());
			cov.saveToFile("coverage.txt");
		});
		p.add(coverageButton);
	}
	
	public int getSelectionRow() {
		return selectionRow;
	}
	
	public void selectPrev() {
		if (selectionRow > 0) {
			selectionRow--;
			owner.updateDisplay();
		}
	}
	
	public void selectNext() {
		if (selectionRow < model.getRowCount() - 1) {
			selectionRow++;
			owner.updateDisplay();
		}
	}
	
	public java.util.List<DecaFourStateConfig> getConfigList() {
		java.util.List<DecaFourStateConfig> result = new ArrayList<DecaFourStateConfig>();
		
		for (int i = 0; i < model.getRowCount(); i++) {
			result.add(getConfigAt(i));
		}
		
		return result;
	}
	
	public DecaFourStateConfig getConfigAt(int row) {
		Map<JScope, DecaFourVertex> vtxs = new HashMap<JScope, DecaFourVertex>();
		
		for (int i = 0; i < owner.getOrderedScopes().size(); i++) {
			JScope scope = owner.getOrderedScopes().get(i);
			vtxs.put(scope, (DecaFourVertex)model.getValueAt(row, i + 1));
		}
		
		return new DecaFourStateConfig(vtxs);
	}
	
	public DecaFourStateConfig getSelectedConfig() {
		return getConfigAt(selectionRow);
	}
	
	public DecaFourStateConfig getSelectedConfigSucc(DecaFourStateConfig fallback) {
		if (selectionRow < model.getRowCount() - 1) {
			return getConfigAt(selectionRow + 1);
		}
		
		return fallback;
	}
	
	public int getRowCount() {
		return model.getRowCount();
	}
	
	public void branch(DecaFourStateConfig c) {
		for (int i = selectionRow + 1; i < model.getRowCount(); i++) {
			model.removeRow(selectionRow + 1);
		}
		
		int row = model.getRowCount();
		model.addRow(createTableRow(row + 1, c));
		model.fireTableDataChanged();
		selectionRow = row;
		
		owner.updateDisplay();
	}
	
	private DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (column > 0) { //Not the time column
				if (row > 0) { //Not the first row
					if (model.getValueAt(row, column).equals(model.getValueAt(row - 1, column))) {
						c.setBackground(Color.WHITE);
					} else {
						c.setBackground(Color.YELLOW);
					}
				} else {
					c.setBackground(Color.WHITE);
				}
			} else {
				c.setBackground(Color.WHITE);
			}
			
			return c;
		}
	};
}

