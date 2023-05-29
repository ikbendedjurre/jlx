package models.pendeltrain;

import jlx.asal.j.JInt;
import jlx.behave.proto.gui.*;
import jlx.common.reflection.ReflectionException;
import jlx.models.Model;
import jlx.models.UnifyingBlock;
import jlx.printing.*;

public class __Run {
	public static void main(String[] args) throws ReflectionException {
		Model m = new Model();
		PendelTrain_SR.Block x = m.add("pendel", PendelTrain_SR.Block.class);
		m.add("train", new Train_STD());
		m.add("infrastructure", new Infrastructure_STD());
		
		//Set the timeout:
//		x.train.D99_timeBetweenStations.restrict(new JInt._1000());
		
		UnifyingBlock ub = new UnifyingBlock("pendel", m, false, false);
		
//		new DecaFourExplorerGUI(ub.sms4);
//		new DecaFourSimulatorGUI(ub.sms4);
//		PrintingOptions options = new PrintingOptions();
//		//options.
//		new MCRL2Printer(ub, options).printAndPop("models/pendel");
	}
}
