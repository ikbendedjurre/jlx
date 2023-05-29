package models.lamp;

import jlx.behave.proto.gui.DecaFourSimulatorGUI;
import jlx.common.reflection.ReflectionException;
import jlx.models.Model;
import jlx.models.UnifyingBlock;
import jlx.printing.MCRL2Printer;
import jlx.printing.PrintingOptions;

public class __Run {
	public static void main(String[] args) throws ReflectionException {
		Model m = new Model();
		m.add("lamp", new Lamp_SR.Block());
		m.add("toggle", new Toggle_STD());
		m.add("lightbulb", new Lightbulb_STD());
		
		UnifyingBlock ub = new UnifyingBlock("lamp", m, false, false);
		
		new MCRL2Printer(ub, new PrintingOptions()).printAndPop("lamp");
		
		new DecaFourSimulatorGUI(ub.sms4);
	}
}

