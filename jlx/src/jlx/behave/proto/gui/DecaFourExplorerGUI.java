package jlx.behave.proto.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import jlx.asal.j.JScope;
import jlx.behave.proto.*;

@SuppressWarnings("serial")
public class DecaFourExplorerGUI extends JFrame {
	private final DecaFourStateMachines sms;
	private final DecaFourStateConfig initConfig;
	private final java.util.List<JScope> orderedScopes;
	private final Map<JScope, ScopeModule> modulePerScope;
	private final DefaultTableModel model;
	private final JButton prevButton;
	private final JLabel indexLabel;
	private final JButton nextButton;
	private final JButton randomButton;
	private final JButton exploreButton;
	private final JButton autoExploreButton;
	private final JButton historyButton;
	private final DecaFourHistoryGUI historyFrame;
	
	private static class ScopeModule {
		public final JLabel currentVertexLabel;
		public final JComboBox<DecaFourVertex> succVerticesDropdown;
		public final DefaultComboBoxModel<DecaFourVertex> succVerticesModel;
		public final JLabel succVtxCountLabel; 
		
		public ScopeModule(JScope scope) {
			currentVertexLabel = new JLabel();
			succVerticesModel = new DefaultComboBoxModel<DecaFourVertex>();
			succVerticesDropdown = new JComboBox<DecaFourVertex>(succVerticesModel);
			succVtxCountLabel = new JLabel();
		}
	}
	
	public DecaFourExplorerGUI(DecaFourStateMachines sms) {
		super("DecaFourStateMachines Explorer");
		
		this.sms = sms;
		
		initConfig = sms.initCfg;
		orderedScopes = Collections.unmodifiableList(sms.orderedScopes);
		modulePerScope = new HashMap<JScope, ScopeModule>();
		
		for (JScope scope : orderedScopes) {
			modulePerScope.put(scope, new ScopeModule(scope));
		}
		
		model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model.addColumn("Output");
		model.addColumn("Current value");
		model.addColumn("Next value");
		
		for (Map.Entry<String, String> e : initConfig.getValuePerOutput().entrySet()) {
			model.addRow(new Object[] { e.getKey(), e.getValue(), e.getValue() });
		}
		
		historyFrame = new DecaFourHistoryGUI(this);
		
		prevButton = new JButton("<<");
		indexLabel = new JLabel();
		nextButton = new JButton(">>");
		
		randomButton = new JButton("Randomize");
		exploreButton = new JButton("Explore next state");
		autoExploreButton = new JButton("Explore 10x");
		historyButton = new JButton("History");
		
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		
		JPanel buttonPanel1 = new JPanel();
		initButtonPanel1(buttonPanel1);
		
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridwidth = 1;
		c1.gridheight = 1;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.weightx = 1;
		c1.weighty = 0;
		c1.gridx = 0;
		c1.gridy = 0;
		main.add(buttonPanel1, c1);
		
		JPanel comboBoxPanel = new JPanel();
		initComboBoxPanel(comboBoxPanel);
		
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridwidth = 1;
		c2.gridheight = 1;
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.weightx = 1;
		c2.weighty = 0;
		c2.gridx = 0;
		c2.gridy = 1;
		main.add(comboBoxPanel, c2);
		
		JPanel buttonPanel2 = new JPanel();
		initButtonPanel2(buttonPanel2);
		
		GridBagConstraints c3 = new GridBagConstraints();
		c3.gridwidth = 1;
		c3.gridheight = 1;
		c3.fill = GridBagConstraints.HORIZONTAL;
		c3.weightx = 1;
		c3.weighty = 0;
		c3.gridx = 0;
		c3.gridy = 2;
		main.add(buttonPanel2, c3);
		
		JPanel tablePanel = new JPanel();
		initTablePanel(tablePanel);
		
		GridBagConstraints c4 = new GridBagConstraints();
		c4.gridwidth = 1;
		c4.gridheight = 1;
		c4.fill = GridBagConstraints.BOTH;
		c4.weightx = 1;
		c4.weighty = 1;
		c4.gridx = 0;
		c4.gridy = 3;
		main.add(tablePanel, c4);
		
		setContentPane(main);
		
		setSize(600, 400);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		updateDisplay();
	}
	
	private void initButtonPanel1(JPanel p) {
		prevButton.addActionListener(e -> {
			historyFrame.selectPrev();
		});
		nextButton.addActionListener(e -> {
			historyFrame.selectNext();
		});
		p.add(prevButton);
		p.add(indexLabel);
		p.add(nextButton);
	}
	
	private final static String SEP = "   ";
	
	private void initComboBoxPanel(JPanel p) {
		p.setLayout(new GridLayout(1 + orderedScopes.size(), 3));
		
		p.add(new JLabel("Current state" + SEP, SwingConstants.RIGHT));
		p.add(new JLabel("Next state"));
		p.add(new JPanel());
		
		for (JScope scope : orderedScopes) {
			ScopeModule m = modulePerScope.get(scope);
			p.add(m.currentVertexLabel);
			p.add(m.succVerticesDropdown);
			p.add(m.succVtxCountLabel);
			
			m.currentVertexLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			m.currentVertexLabel.setFont(m.currentVertexLabel.getFont().deriveFont(Font.PLAIN));
			m.succVerticesDropdown.addItemListener(comboBoxListener);
			m.succVerticesDropdown.setFont(m.succVerticesDropdown.getFont().deriveFont(Font.PLAIN));
			m.succVtxCountLabel.setFont(m.succVtxCountLabel.getFont().deriveFont(Font.PLAIN));
		}
	}
	
	private void initButtonPanel2(JPanel p) {
		randomButton.addActionListener(e -> {
			randomize();
		});
		exploreButton.addActionListener(e -> {
			explore();
		});
		autoExploreButton.addActionListener(e -> {
			for (int i = 1; i <= 10; i++) {
				randomize();
				explore();
			}
		});
		historyButton.addActionListener(e -> {
			historyFrame.setVisible(true);
			historyFrame.requestFocus();
		});
		p.add(randomButton);
		p.add(exploreButton);
		p.add(autoExploreButton);
		p.add(historyButton);
	}
	
	private void initTablePanel(JPanel p) {
		p.setLayout(new BorderLayout());
		
		JTable table = new JTable(model);
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		
		for (int i = 0; i <  table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(tableRenderer);
		}
		
		JScrollPane scrollPane = new JScrollPane(table);
		p.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void updateDisplay() {
		DecaFourStateConfig c = historyFrame.getSelectedConfig();
		Map<JScope, DecaFourVertex> preferredSuccVtxs = new HashMap<JScope, DecaFourVertex>();
		preferredSuccVtxs.putAll(historyFrame.getSelectedConfigSucc(c).getVtxs());
		
		Map<JScope, java.util.List<DecaFourVertex>> succVtxsPerScope = c.computeOrderedSuccsPerScope();
		
		for (Map.Entry<JScope, DecaFourVertex> e : c.getVtxs().entrySet()) {
			ScopeModule m = modulePerScope.get(e.getKey());
			m.currentVertexLabel.setText(e.getValue().getName() + SEP);
			m.succVerticesModel.removeAllElements();
			
			java.util.List<DecaFourVertex> succVtxs = succVtxsPerScope.get(e.getKey());
			m.succVerticesModel.addAll(succVtxs);
			m.succVtxCountLabel.setText(" (" + succVtxs.size() + ")");
			
			if (!succVtxs.contains(preferredSuccVtxs.get(e.getKey()))) {
				preferredSuccVtxs.put(e.getKey(), succVtxs.get(0));
			}
		}
		
		indexLabel.setText((historyFrame.getSelectionRow() + 1) + " / " + historyFrame.getRowCount());
		
		Map<String, String> valuePerOutput = c.getValuePerOutput();
		
		for (int row = 0; row < model.getRowCount(); row++) {
			String output = (String)model.getValueAt(row, 0);
			model.setValueAt(valuePerOutput.get(output), row, 1);
		}
		
		//Model must fire an update, this happens by setSucc:
		updateSuccDisplay(new DecaFourStateConfig(preferredSuccVtxs));
	}
	
	private void updateSuccDisplay(DecaFourStateConfig succ) {
		if (!comboBoxBusy) {
			comboBoxBusy = true;
			
			for (Map.Entry<JScope, DecaFourVertex> e : succ.getVtxs().entrySet()) {
				ScopeModule m = modulePerScope.get(e.getKey());
				m.succVerticesModel.setSelectedItem(e.getValue());
			}
			
			comboBoxBusy = false;
		}
		
		Map<String, String> valuePerOutput = succ.getValuePerOutput();
		
		for (int row = 0; row < model.getRowCount(); row++) {
			String output = (String)model.getValueAt(row, 0);
			model.setValueAt(valuePerOutput.get(output), row, 2);
		}
		
		model.fireTableDataChanged();
	}
	
	private DecaFourStateConfig constructSucc() {
		Map<JScope, DecaFourVertex> vtxs = new HashMap<JScope, DecaFourVertex>();
		
		for (Map.Entry<JScope, ScopeModule> mps : modulePerScope.entrySet()) {
			DecaFourVertex vtx = (DecaFourVertex)mps.getValue().succVerticesModel.getSelectedItem();
			vtxs.put(mps.getKey(), vtx);
		}
		
		return new DecaFourStateConfig(vtxs);
	}
	
	public DecaFourStateMachines getStateMachines() {
		return sms;
	}
	
	public DecaFourStateConfig getInitConfig() {
		return initConfig;
	}
	
	public java.util.List<JScope> getOrderedScopes() {
		return orderedScopes;
	}
	
	private void randomize() {
		Map<JScope, DecaFourVertex> vtxs = new HashMap<JScope, DecaFourVertex>();
		
		for (Map.Entry<JScope, ScopeModule> mps : modulePerScope.entrySet()) {
			int r = (int)(Math.random() * mps.getValue().succVerticesModel.getSize());
			DecaFourVertex v = mps.getValue().succVerticesModel.getElementAt(r);
			mps.getValue().succVerticesModel.setSelectedItem(v);
			vtxs.put(mps.getKey(), v);
		}
		
		updateSuccDisplay(new DecaFourStateConfig(vtxs));
	}
	
	private void explore() {
		historyFrame.branch(constructSucc());
	}
	
	private boolean comboBoxBusy = false;
	private ItemListener comboBoxListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (!comboBoxBusy) {
				if (e.getID() == ItemEvent.ITEM_STATE_CHANGED) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						comboBoxBusy = true;
						updateSuccDisplay(constructSucc());
						comboBoxBusy = false;
					}
				}
			}
		}
	};
	
	private DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (model.getValueAt(row, 1).equals(model.getValueAt(row, 2))) {
				c.setBackground(Color.WHITE);
			} else {
				c.setBackground(Color.YELLOW);
			}
			
			return c;
		}
	};
}

