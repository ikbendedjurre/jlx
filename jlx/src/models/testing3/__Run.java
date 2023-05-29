package models.testing3;

import jlx.common.reflection.*;
import jlx.models.*;
import jlx.printing.MCRL2Printer;
import jlx.printing.PrintingOptions;

public class __Run {
	public static void main2(String[] args) throws ReflectionException {
		Model m = new Model();
		m.add("one_logic_elem", new OneLogicElemTestIBD2.Block());
		OrSMD or = m.add("or", new OrSMD());
		
		UnifyingBlock ub = new UnifyingBlock("test", m, true, true);
		ub.makeEnvInputPulsePortsSynchronous();
		PrintingOptions options = PrintingOptions.empty();
		
		new MCRL2Printer(ub, options).printAndPop("one_logic_elem");
	}
	
	public static void main(String[] args) throws ReflectionException {
		Model m = new Model();
		m.add("simple_logic_test", new SimpleLogicTestIBD2.Block());
		OrSMD or = m.add("or", new OrSMD());
		AndSMD and = m.add("and", new AndSMD());
		
		UnifyingBlock ub = new UnifyingBlock("test", m, true, true);
		PrintingOptions options = PrintingOptions.empty();
		options.RESET_VARIABLES_INITIAL = true;
		options.SEQ_EXECUTION_TOGGLES = true;
		
		new MCRL2Printer(ub, options).printAndPop("simple_logic_test");
		
		
//		System.out.println("#reprBlocks = " + ub.reprBlocks.size());
//		System.out.println("#reprPorts = " + ub.reprPorts.size());
//		System.out.println("#reprProps = " + ub.reprProperties.size());
//		
//		for (ReprPort rp : ub.reprPorts) {
//			System.out.println(rp.name);
//		}
//		
//		for (TritoStateMachine rsm : ub.reprStateMachines) {
//			for (TritoTransition t : rsm.getAllTransitions()) {
//				System.out.println("{");
//				System.out.println("\t" + t.getStatement().textify(LOD.FULL));
//				System.out.println("}");
//				
//				if (t.getStatement() instanceof ASALSeqStatement) {
//					ASALSeqStatement seq = (ASALSeqStatement)t.getStatement();
//					
//					if (seq.getStatement() instanceof ASALAssignStatement) {
//						ASALAssignStatement a = (ASALAssignStatement)seq.getStatement();
//						
//						if (ub.reprPortPerType.get(a.getResolvedVar().getLegacy()) != null) {
//							System.out.println("resolved");
//						} else {
//							System.out.println("unresolved");
//						}
//					} else {
//						System.out.println("not an assignment but " + t.getStatement().getClass().getCanonicalName());
//					}
//				} else {
//					System.out.println("not a seq but " + t.getStatement().getClass().getCanonicalName());
//				}
//			}
//		}
		
		
	}
}
