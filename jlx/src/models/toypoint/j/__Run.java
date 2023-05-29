package models.toypoint.j;

import jlx.asal.j.JInt;
import jlx.behave.proto.gui.*;
import jlx.behave.verify.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.common.reflection.ReflectionException;
import jlx.models.Model;
import jlx.models.UnifyingBlock;
import jlx.printing.*;
import models.pendeltrain.j.scen.*;

public class __Run {
	public static void main(String[] args) throws ReflectionException {
		Model m = new Model();
		//Point_SR.Block x = m.add("p", Point_SR.Block.class);
		Point_SR.Block x = m.add("p", new Point_STD());
//		m.add("infrastructure", new Infrastructure_STD());
		
		//Set the timeout:
//		x.train.D99_timeBetweenStations.restrict(new JInt._1000());
		x.D60_timeout_duration.restrict(new JInt._1000());
		x.DT1_move_pos.restrict(EndPos.LEFT, EndPos.RIGHT);
		
//		VerificationModel vm = new VerificationModel();
//		x.infrastructure.D1_trainPos.getVerificationModels().add(vm);
//		x.infrastructure.D2_signal1.getVerificationModels().add(vm);
//		x.infrastructure.D3_signal2.getVerificationModels().add(vm);
		
		UnifyingBlock ub = new UnifyingBlock("p", m, true, true);
//		NikolaGraph maker = new NikolaGraph(ub, vm);
		
//		NikolaExporter exporter = new NikolaExporter(maker);
//		exporter.export();
		
//		new DecaFourExplorerGUI(ub.sms4);
//		new DecaFourSimulatorGUI(ub.sms4);
//		new MCRL2Printer(ub, new PrintingOptions()).printAndPop("models/pendelj");
//		DecaFourScenarioPlayer player = new DecaFourScenarioPlayer(ub);
//		player.play(new ScenarioBackAndForth(x), null);
//		player.play(new ScenarioBreakdownAndRepair(x), null);
//		player.play(new ScenarioBreakdownAndRepairInTransit(x), null);
	}
}
