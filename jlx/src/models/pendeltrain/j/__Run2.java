package models.pendeltrain.j;

import jlx.asal.j.JInt;
import jlx.behave.proto.gui.*;
import jlx.behave.verify.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.common.reflection.ReflectionException;
import jlx.models.Model;
import jlx.models.UnifyingBlock;
import jlx.printing.*;
import models.pendeltrain.j.scen.*;

public class __Run2 {
	public static void main(String[] args) throws ReflectionException {
		Model m = new Model();
		m.add("pendelj", PendelTrain_SR.Block.class);
		m.add("train", new Train_STD());
		
		UnifyingBlock ub = new UnifyingBlock("pendelj", m, true, false);
		
		new DecaFourSimulatorGUI(ub.sms4);
	}
}
