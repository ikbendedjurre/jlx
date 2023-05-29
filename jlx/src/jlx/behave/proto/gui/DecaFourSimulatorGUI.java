package jlx.behave.proto.gui;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.*;
import jlx.utils.*;

@SuppressWarnings("serial")
public class DecaFourSimulatorGUI extends JFrame {
	private final static int MODULE_COLUMN = 1;
	private final static int CURRENT_VALUE_COLUMN = 2;
	private final static int NEXT_VALUE_COLUMN = 3;
	
	private final DecaFourStateMachines sms;
	private final DefaultTableModel inputModel;
	private final DefaultTableModel outputModel;
	private final DefaultComboBoxModel<SuccModule> succModel;
	private final DefaultTableModel historyModel;
	private final java.util.List<OutputModule> orderedOutputModules;
	private final java.util.List<BlockModule> orderedBlockModules;
	private final JTable historyTable;
	private final JButton prevButton;
	private final JLabel indexLabel;
	private final JButton nextButton;
	private final JButton stepButton;
	private final JButton stabilizeButton;
	private final JButton exportButton;
	
	private java.util.List<UserSimStep> userSteps;
	private java.util.List<DecaFourStateConfig> cfgSeq;
	private java.util.List<PulsePackMap> inputValSeq;
	private int currIndex;
	
	private static class InputModule {
		public final ReprPort input;
		
		public InputModule(ReprPort input) {
			this.input = input;
		}
		
		@Override
		public String toString() {
			return input.getName();
		}
	}
	
	private static class OutputModule {
		public final ReprBlock owner;
		public final Object output;
		public final String outputName;
		public final boolean external;
		
		public OutputModule(ReprBlock owner, Object output, boolean external) {
			this.owner = owner;
			this.output = output;
			this.external = external;
			
			if (output instanceof JScope) {
				outputName = "<state>";
			} else {
				if (output instanceof ReprPort) {
					outputName = ((ReprPort)output).getName();
				} else {
					throw new Error("Should not happen!");
				}
			}
		}
		
		public Object getValue(DecaFourStateConfig cfg) {
			if (output instanceof JScope) {
				return cfg.getVtxs().get(owner);
			}
			
			if (output instanceof ReprPort) {
				ReprPort rp = (ReprPort)output;
				return cfg.getOutputVal().getPortValue(rp, false);
			}
			
			throw new Error("Should not happen!");
		}
		
		@Override
		public String toString() {
			return outputName;
		}
	}
	
	private static class SuccModule {
		public final String name;
		public final DecaFourStateConfig cfg;
		
		public SuccModule(String name, DecaFourStateConfig cfg) {
			this.name = name;
			this.cfg = cfg;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static class BlockModule {
		public final String name;
		public final ReprBlock block;
		
		public BlockModule(String name, ReprBlock block) {
			this.name = name;
			this.block = block;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public DecaFourSimulatorGUI(DecaFourStateMachines sms) {
		super((sms.name != null ? "[" + sms.name + "] " : "") + "Simulator");
		
		this.sms = sms;
		
		TextOptions.select(TextOptions.FULL);
		
		userSteps = new ArrayList<UserSimStep>();
		userSteps.add(UserSimStep.INITIALIZATION);
		cfgSeq = new ArrayList<DecaFourStateConfig>();
		cfgSeq.add(sms.initCfg);
		inputValSeq = new ArrayList<PulsePackMap>();
		inputValSeq.add(sms.initialInputs.extractExternalMap());
		orderedOutputModules = new ArrayList<OutputModule>();
		orderedBlockModules = new ArrayList<BlockModule>();
		currIndex = 0;
		
		inputModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == NEXT_VALUE_COLUMN;
			}
		};
		inputModel.addColumn("Block"); //String
		inputModel.addColumn("Input port"); //InputModule
		inputModel.addColumn("Current value"); //ASALSymbolicValue
		inputModel.addColumn("Next value"); //ASALSymbolicValue
		
		for (JScope scope : sms.orderedScopes) {
			ReprBlock rb = (ReprBlock)scope;
			
			for (Map.Entry<String, ASALVariable> e : scope.getVariablePerName().entrySet()) {
				if (e.getValue() instanceof ReprPort) {
					ReprPort rp = (ReprPort)e.getValue();
					
					if (rp.getDir() == Dir.IN && rp.isPortToEnvironment()) {
						ASALSymbolicValue v = sms.initialInputs.getPortValue(rp, false);
						inputModel.addRow(new Object[] { rb.getName(), new InputModule(rp), v, v });
					}
				}
			}
		}
		
		outputModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		outputModel.addColumn("Block"); //String
		outputModel.addColumn("Output port"); //OutputModule
		outputModel.addColumn("Current value"); //Object
		outputModel.addColumn("Next value"); //Object
		
		for (JScope scope : sms.orderedScopes) {
			ReprBlock rb = (ReprBlock)scope;
			
			{
				OutputModule m = new OutputModule(rb, rb, false);
				Object v = m.getValue(sms.initCfg);
				outputModel.addRow(new Object[] { rb.getName(), m, v, v });
				orderedOutputModules.add(m);
			}
			
			for (Map.Entry<String, ASALVariable> e : scope.getVariablePerName().entrySet()) {
				if (e.getValue() instanceof ReprPort) {
					ReprPort rp = (ReprPort)e.getValue();
					
					if (rp.getDir() == Dir.OUT) {
						OutputModule m = new OutputModule(rb, rp, rp.isPortToEnvironment());
						Object v = m.getValue(sms.initCfg);
						outputModel.addRow(new Object[] { rb.getName(), m, v, v });
						orderedOutputModules.add(m);
					}
				}
			}
		}
		
		succModel = new DefaultComboBoxModel<SuccModule>();
		
		historyModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		historyModel.addColumn("Step"); //Integer
		
		for (JScope scope : sms.orderedScopes) {
			ReprBlock rb = (ReprBlock)scope;
			orderedBlockModules.add(new BlockModule(rb.getName(), rb));
			historyModel.addColumn(rb.getName() + "::" + rb.getStateMachine().getClass().getSimpleName());
		}
		
		historyTable = new JTable(historyModel);
		prevButton = new JButton("<<");
		indexLabel = new JLabel("");
		nextButton = new JButton(">>");
		stepButton = new JButton("Step");
		stabilizeButton = new JButton("Stabilize");
		exportButton = new JButton("Copy to clipboard as scenario");
		
		JPanel inputControlPanel = new JPanel();
		initInputControlPanel(inputControlPanel);
		
		JPanel inputPanel = new JPanel();
		initInputPanel(inputPanel, inputControlPanel);
		
		JPanel outputControlPanel = new JPanel();
		initOutputControlPanel(outputControlPanel);
		
		JPanel outputPanel = new JPanel();
		initOutputPanel(outputPanel, outputControlPanel);
		
		JSplitPane inputOutputPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputPanel);
		inputOutputPanel.setResizeWeight(0.5);
		inputOutputPanel.setOneTouchExpandable(true);
		inputOutputPanel.setContinuousLayout(true);
		
		JPanel historyControlPanel = new JPanel();
		initHistoryControlPanel(historyControlPanel);
		
		JPanel historyPanel = new JPanel();
		initHistoryPanel(historyPanel, historyControlPanel);
		
		JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputOutputPanel, historyPanel);
		main.setResizeWeight(0.5);
		main.setOneTouchExpandable(true);
		main.setContinuousLayout(true);
		
		setContentPane(main);
		
		setSize(600, 400);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		updateCurrent();
		updateHistory();
	}
	
	public java.util.List<UserSimStep> getUserSteps() {
		return userSteps;
	}
	
	public java.util.List<DecaFourStateConfig> getCfgSeq() {
		return cfgSeq;
	}
	
	public java.util.List<PulsePackMap> getInputValSeq() {
		return inputValSeq;
	}
	
	public int getCurrIndex() {
		return currIndex;
	}
	
//	private InputVal extractInitialInputVal() {
//		Map<ReprPort, ASALSymbolicValue> valuePerPort = new HashMap<ReprPort, ASALSymbolicValue>();
//		
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : sms.initialInputs.getValuePerPort().entrySet()) {
//			if (e.getKey().isPortToEnvironment()) {
//				valuePerPort.put(e.getKey(), e.getValue());
//			}
//		}
//		
//		return new InputVal(valuePerPort);
//	}
	
	private void initInputPanel(JPanel p, JPanel controlPanel) {
		p.setLayout(new BorderLayout());
		p.add(controlPanel, BorderLayout.PAGE_START);
		
		JTable table = new JTable(inputModel);
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.getColumnModel().getColumn(NEXT_VALUE_COLUMN).setCellEditor(inputTableCellEditor);
		table.setDefaultRenderer(Object.class, inputTableCellRenderer);
		
		JScrollPane scrollPane = new JScrollPane(table);
		p.add(scrollPane, BorderLayout.CENTER);
	}
	
	private void initOutputPanel(JPanel p, JPanel controlPanel) {
		p.setLayout(new BorderLayout());
		p.add(controlPanel, BorderLayout.PAGE_START);
		
		JTable table = new JTable(outputModel);
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(outputTableCellRenderer);
		}
		
		JScrollPane scrollPane = new JScrollPane(table);
		p.add(scrollPane, BorderLayout.CENTER);
	}
	
	private void initInputControlPanel(JPanel p) {
		prevButton.addActionListener(this::prev);
		nextButton.addActionListener(this::next);
		p.add(prevButton);
		p.add(indexLabel);
		p.add(nextButton);
	}
	
	public void prev(ActionEvent e) {
		for (int i = currIndex - 1; i >= 0; i--) {
			if (!userSteps.get(i).equals(UserSimStep.UNSTABLE)) {
				currIndex = i;
				updateCurrent();
				historyTable.repaint();
				return;
			}
		}
	}
	
	public void next(ActionEvent e) {
		for (int i = currIndex + 1; i < userSteps.size(); i++) {
			if (!userSteps.get(i).equals(UserSimStep.UNSTABLE)) {
				currIndex = i;
				updateCurrent();
				historyTable.repaint();
				return;
			}
		}
	}
	
	private void initOutputControlPanel(JPanel p) {
		JComboBox<SuccModule> succBox = new JComboBox<SuccModule>(succModel);
		succBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateOutputs();
			}
		});
		
		stepButton.addActionListener(this::step);
		stabilizeButton.addActionListener(this::stabilize);
		
		p.add(succBox);
		p.add(stepButton);
		p.add(stabilizeButton);
	}
	
	private void initHistoryControlPanel(JPanel p) {
		exportButton.addActionListener(this::export);
		
		p.add(exportButton);
	}
	
	private void initHistoryPanel(JPanel p, JPanel controlPanel) {
		p.setLayout(new BorderLayout());
		
		JPanel headerPanel = new JPanel();
		headerPanel.add(new JLabel("History"));
		p.add(headerPanel, BorderLayout.PAGE_START);
		
		JTable table = historyTable;
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.setDefaultRenderer(Object.class, historyTableCellRenderer);
		
		JScrollPane scrollPane = new JScrollPane(table);
		p.add(scrollPane, BorderLayout.CENTER);
		
		p.add(controlPanel, BorderLayout.PAGE_END);
	}
	
	private void updateCurrent() {
		indexLabel.setText(String.format("Step %d inputs", currIndex + 1));
		
		DecaFourStateConfig cfg = cfgSeq.get(currIndex);
		System.out.println("cfg[" + currIndex + "] =\n\t\t" + cfg.getDescription("\n\t\t"));
		
		updateInputs();
		updateSuccs();
	}
	
	private void updateInputs() {
		PulsePackMap inputVal1 = inputValSeq.get(currIndex);
		PulsePackMap inputVal2;
		
		if (currIndex + 1 < inputValSeq.size()) {
			inputVal2 = inputValSeq.get(currIndex + 1);
		} else {
			inputVal2 = inputVal1.deactivate();
		}
		
		for (int row = 0; row < inputModel.getRowCount(); row++) {
			InputModule m = (InputModule)inputModel.getValueAt(row, MODULE_COLUMN);
			inputModel.setValueAt(inputVal1.getPortValue(m.input, false), row, CURRENT_VALUE_COLUMN);
			inputModel.setValueAt(inputVal2.getPortValue(m.input, false), row, NEXT_VALUE_COLUMN);
		}
		
		inputModel.fireTableDataChanged();
	}
	
	private void updateSuccs() {
		DecaFourStateConfig cfg = cfgSeq.get(currIndex);
		PulsePackMap inputVal = PulsePackMap.from(extractInputVal().extractValuation(), Dir.IN);
		java.util.List<DecaFourStateConfig> succs = new ArrayList<DecaFourStateConfig>(cfg.computeSuccsViaInputVal(inputVal));
		
		if (succs.isEmpty()) {
			for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.extractValuation().entrySet()) {
				System.out.println(e.getKey().getName() + " = " + e.getValue());
			}
			
			throw new Error("Should not happen!");
		}
		
		succModel.removeAllElements();
		
		for (DecaFourStateConfig succ : succs) {
			succModel.addElement(new SuccModule(String.format("Possibility %d of %d", succModel.getSize() + 1, succs.size()), succ));
		}
		
		succModel.setSelectedItem(succModel.getElementAt(0)); //Triggers 'updateOutputs' indirectly.
	}
	
	private void updateOutputs() {
		SuccModule selectedModule = (SuccModule)succModel.getSelectedItem();
		
		if (selectedModule != null) { //This can happen when GUI events are triggered.
			DecaFourStateConfig cfg = cfgSeq.get(currIndex);
			
			for (int row = 0; row < outputModel.getRowCount(); row++) {
				OutputModule m = (OutputModule)outputModel.getValueAt(row, MODULE_COLUMN);
				outputModel.setValueAt(m.getValue(cfg), row, CURRENT_VALUE_COLUMN);
				outputModel.setValueAt(m.getValue(selectedModule.cfg), row, NEXT_VALUE_COLUMN);
			}
			
			outputModel.fireTableDataChanged();
			
			updateHistory();
		}
	}
	
	private PulsePackMap extractInputVal() {
		Map<ReprPort, ASALSymbolicValue> valuePerPort = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (int row = 0; row < inputModel.getRowCount(); row++) {
			InputModule m = (InputModule)inputModel.getValueAt(row, MODULE_COLUMN);
			valuePerPort.put(m.input, (ASALSymbolicValue)inputModel.getValueAt(row, NEXT_VALUE_COLUMN));
		}
		
		return PulsePackMap.from(valuePerPort, Dir.IN);
	}
	
	private void updateHistory() {
		for (int row = historyModel.getRowCount() - 1; row >= 0; row--) {
			historyModel.removeRow(row);
		}
		
		for (int step = 0; step < cfgSeq.size(); step++) {
			PulsePackMap inputVal1 = inputValSeq.get(step);
			PulsePackMap inputVal2;
			DecaFourStateConfig cfg1 = cfgSeq.get(step);
			DecaFourStateConfig cfg2;
			
			if (step + 1 < cfgSeq.size()) {
				inputVal2 = inputValSeq.get(step + 1);
				cfg2 = cfgSeq.get(step + 1);
			} else {
				if (currIndex == cfgSeq.size() - 1) {
					inputVal2 = extractInputVal();
					cfg2 = ((SuccModule)succModel.getSelectedItem()).cfg;
				} else {
					inputVal2 = inputVal1.deactivate();
					cfg2 = cfg1;
				}
			}
			
			//DecaFourStateConfig cfg = cfgSeq.get(step);
			Object[] row = new Object[1 + orderedBlockModules.size()];
			
			if (userSteps.get(step) != UserSimStep.UNSTABLE) {
				row[0] = (step + 1) + ": " + userSteps.get(step);
			} else {
				row[0] = (step + 1);
			}
			
			for (int i = 0; i < orderedBlockModules.size(); i++) {
				BlockModule m = orderedBlockModules.get(i);
				java.util.List<String> changedInputs = new ArrayList<String>();
				
				for (Map.Entry<ReprPort, PulsePack> e : inputVal2.extractEventMap(inputVal1).getPackPerPort().entrySet()) {
					if (e.getKey().getReprOwner() == m.block) {
						for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : e.getValue().getValuePerPort().entrySet()) {
							changedInputs.add(TextOptions.MINIMAL.id(e2.getKey().getName()) + " := " + e2.getValue() + ";");
						}
					}
				}
				
				java.util.List<String> changedOutputs = new ArrayList<String>();
				
				for (Map.Entry<ReprPort, PulsePack> e : cfg2.getOutputVal().extractEventMap(cfg1.getOutputVal()).getPackPerPort().entrySet()) {
					if (e.getKey().getReprOwner() == m.block) {
						for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : e.getValue().getValuePerPort().entrySet()) {
							changedOutputs.add(TextOptions.MINIMAL.id(e2.getKey().getName()) + " := " + e2.getValue() + ";");
						}
					}
				}
				
				row[i + 1] = Texts.concat(changedInputs, " ") + " / " + Texts.concat(changedOutputs, " ");
			}
			
			historyModel.addRow(row);
		}
		
		historyModel.fireTableDataChanged();
	}
	
	private void clearAfterCurrent() {
		int lastIndex = cfgSeq.size() - 1;
		
		while (lastIndex > currIndex) {
			cfgSeq.remove(lastIndex);
			inputValSeq.remove(lastIndex);
			userSteps.remove(lastIndex);
			lastIndex = cfgSeq.size() - 1;
		}
	}
	
	public void step(ActionEvent e) {
		clearAfterCurrent();
		
		currIndex++;
		cfgSeq.add(((SuccModule)succModel.getSelectedItem()).cfg);
		
		userSteps.add(UserSimStep.STEP);
		inputValSeq.add(extractInputVal());
		
		updateCurrent();
		updateHistory();
	}
	
	public void stabilize(ActionEvent e) {
		if (userSteps.get(currIndex) == UserSimStep.STABILIZED) {
			if (extractInputVal().equals(inputValSeq.get(currIndex))) {
				JOptionPane.showMessageDialog(this, "Already stable.");
				return;
			}
		}
		
		DecaFourStateConfig cfg = cfgSeq.get(currIndex);
		
		PulsePackMap inputVal = PulsePackMap.from(extractInputVal().extractValuation(), Dir.IN);
		PulsePackMap deactivatedInputs = inputVal.deactivate();
		
		Set<DecaFourStateConfig> succs = cfg.computeSuccsViaInputVal(inputVal);
		java.util.List<DecaFourStateConfig> stabilizingSeq = DecaFourStateConfig.followInputs(succs, deactivatedInputs, inputVal).getCfgs();
		
		if (stabilizingSeq == null) {
			//Blocks:
			JOptionPane.showMessageDialog(this, "No stabilizing sequence. Possible reasons:\n1. Cycles.\n2. Distinct non-deterministic output.\n3. Multiple final states.");
			return;
		}
		
		clearAfterCurrent();
		
		currIndex += 1 + stabilizingSeq.size();
		cfgSeq.add(succs.iterator().next());
		cfgSeq.addAll(stabilizingSeq);
		
		inputValSeq.add(inputVal);
		
		for (int index = 1; index <= stabilizingSeq.size(); index++) {
			userSteps.add(UserSimStep.UNSTABLE);
			inputValSeq.add(deactivatedInputs);
		}
		
		userSteps.add(UserSimStep.STABILIZED);
		
		updateCurrent();
		updateHistory();
	}
	
	public void export(ActionEvent e) {
		DecaFourScenarioExport x = new DecaFourScenarioExport(sms.orderedScopes, userSteps, inputValSeq, cfgSeq);
		StringSelection stringSelection = new StringSelection(Texts.concat(x.extractScenario(), "\n"));
		java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	private ReprPortValueTableCellEditor inputTableCellEditor = new ReprPortValueTableCellEditor() {
		@Override
		public ReprPort getPort(int row) {
			return ((InputModule)inputModel.getValueAt(row, MODULE_COLUMN)).input;
		}
		
		@Override
		public void setPort(int row, ASALSymbolicValue value) {
			inputModel.setValueAt(value, row, NEXT_VALUE_COLUMN);
			inputModel.fireTableRowsUpdated(row, row);
			updateSuccs();
		}
	};
	
	private DefaultTableCellRenderer inputTableCellRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (isSelected) {
				c.setBackground(Color.ORANGE);
			} else {
				if (inputModel.getValueAt(row, CURRENT_VALUE_COLUMN).equals(inputModel.getValueAt(row, NEXT_VALUE_COLUMN))) {
					c.setBackground(Color.WHITE);
				} else {
					c.setBackground(Color.GREEN);
				}
			}
			
			InputModule m = (InputModule)inputModel.getValueAt(row, MODULE_COLUMN);
			
			if (m.input.getPossibleValues().size() > 1) {
				c.setForeground(Color.BLACK);
			} else {
				c.setForeground(Color.GRAY);
			}
			
			return c;
		}
	};
	
	private DefaultTableCellRenderer outputTableCellRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (outputModel.getValueAt(row, CURRENT_VALUE_COLUMN).equals(outputModel.getValueAt(row, NEXT_VALUE_COLUMN))) {
				c.setBackground(Color.WHITE);
			} else {
				c.setBackground(Color.YELLOW);
			}
			
			OutputModule m = (OutputModule)outputModel.getValueAt(row, MODULE_COLUMN);
			
			if (m.external) {
				c.setForeground(Color.BLACK);
			} else {
				c.setForeground(Color.GRAY);
			}
			
			return c;
		}
	};
	
	private DefaultTableCellRenderer historyTableCellRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (row == currIndex) {
				c.setBackground(Color.CYAN);
			} else {
				c.setBackground(Color.WHITE);
			}
			
			return c;
		}
	};
}
