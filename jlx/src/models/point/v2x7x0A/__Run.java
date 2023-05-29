package models.point.v2x7x0A;

import java.io.IOException;

import jlx.asal.j.*;
import jlx.behave.proto.gui.*;
import jlx.behave.verify.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.common.reflection.ReflectionException;
import jlx.models.Model;
import jlx.models.UnifyingBlock;
import jlx.printing.*;
import models.generic.types.*;
import models.generic.v3x0x0A.*;
import models.point.types.*;

public class __Run {
	public static void main(String[] args) throws ReflectionException, IOException {
		VerificationModel vm = new VerificationModel("P");
		
		Model m = new Model();
		SCI_P_PDI_SR.Block p = m.add("pdi", new SCI_P_PDI_SR.Block()); //Must match name in EfeS_SCI_XX_Protocol_Stack_SR!
		SubS_P_SR.Block fe = m.add("fe", new SubS_P_SR.Block()); //Must match name in EfeS_SCI_XX_Functional_Connection_Domain_Context!
		EfeS_SCI_XX_Functional_Connection_Domain_Context.Block ctx = m.add("ctx", new EfeS_SCI_XX_Functional_Connection_Domain_Context.Block()); //Any name is fine.
		
		//We do not use the SCP components:
//		EfeS_SCI_XX_Protocol_Stack_SR.Block ps = m.add("ps", new EfeS_SCI_XX_Protocol_Stack_SR.Block()); //Any name is fine.
		//But we still need to connect via the SCI-XX interface:
		EfeS_SCI_XX_PDI_SR.Block pdi = m.add("pdi", new EfeS_SCI_XX_PDI_SR.Block());
		
////		m.add("sp", new SCI_P_STD_1());
		m.add("fp", new SCI_P_STD_2());
////		m.add("prim", new SCI_EfeS_Prim_STD_1());
		m.add("sec", new SCI_EfeS_Sec_STD_1());
		m.add("p3", new F_P3_SR_STD_1());
		m.add("est", new EST_EfeS_STD_2());
		
		fe.p3.D34_Con_008000.setInitialValue(JBool.FALSE);
		fe.p3.D20_F_EST_EfeS_Gen_SR_State.setInitialValue(new EST_EfeS_State.NO_OPERATING_VOLTAGE());
		fe.p3.DT1_Move_Target.setInitialValue(PointPos.LEFT);
		fe.p3.D21_PM1_Position.setInitialValue(PointPos.NO_END_POSITION);
		fe.p3.D22_PM2_Position.setInitialValue(PointPos.NO_END_POSITION);
		
		//Restrictions on triggers:
		fe.est.T1_Power_On_Detected.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.est.T2_Power_Off_Detected.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.est.T3_Reset.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.est.T4_Booted.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.est.T5_SIL_Not_Fulfilled.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.est.T7_Invalid_Or_Missing_Basic_Data.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.est.T9_PDI_Connection_Established.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.est.T16_Data_Installation_Complete.restrict(JPulse.FALSE);
		fe.est.T17_Data_Update_Finished.restrict(JPulse.FALSE);
		p.prim.T20_Protocol_Error.restrict(JPulse.TRUE, JPulse.FALSE);
		p.prim.T21_Formal_Telegram_Error.restrict(JPulse.TRUE, JPulse.FALSE);
		p.prim.T22_Content_Telegram_Error.restrict(JPulse.TRUE, JPulse.FALSE);
		p.prim.T10_SCP_Connection_Terminated.restrict(JPulse.TRUE, JPulse.FALSE);
		p.prim.T5_SCP_Connection_Established.restrict(JPulse.TRUE, JPulse.FALSE);
		p.sec.T20_Protocol_Error.restrict(JPulse.TRUE, JPulse.FALSE);
		p.sec.T21_Formal_Telegram_Error.restrict(JPulse.TRUE, JPulse.FALSE);
		p.sec.T22_Content_Telegram_Error.restrict(JPulse.TRUE, JPulse.FALSE);
		p.sec.T10_SCP_Connection_Terminated.restrict(JPulse.TRUE, JPulse.FALSE);
		p.sec.T5_SCP_Connection_Established.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.smi.T10_Data_Valid.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.smi.T11_Data_Invalid.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.smi.T12_Data_Installation_Successfully.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.smi.T6_Data_Up_To_Date.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.smi.T7_Data_Not_Up_To_Date.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.smi.T8_Data.restrict(JPulse.TRUE, JPulse.FALSE);
		fe.smi.T9_Transmission_Complete.restrict(JPulse.TRUE, JPulse.FALSE);
		p.sp.T10_Move_Point.restrict(JPulse.TRUE, JPulse.FALSE);
		
		//Restrictions on data:
		fe.p3.D13_PM2_Activation.restrict(PM2_Activation.ACTIVE);
		fe.p3.D21_PM1_Position.restrict(PointPos.LEFT, PointPos.RIGHT, PointPos.NO_END_POSITION, PointPos.TRAILED);
		fe.p3.D22_PM2_Position.restrict(PointPos.LEFT, PointPos.RIGHT, PointPos.NO_END_POSITION, PointPos.TRAILED);
		fe.p3.D30_Con_007000.restrict(JBool.FALSE); //??
		fe.p3.D32_Con_007600.restrict(JBool.FALSE); //Bane Nor
		fe.p3.D33_Con_007900.restrict(JBool.FALSE); //SZ
		fe.p3.D34_Con_008000.restrict(JBool.TRUE); //DB Netz
		fe.p3.D35_Con_008200.restrict(JBool.FALSE); //CFL
		fe.p3.D36_Con_008300.restrict(JBool.FALSE); //RFI
		fe.p3.D37_Con_008400.restrict(JBool.FALSE); //ProRail
		fe.p3.D38_Con_008500.restrict(JBool.FALSE); //SBB
		fe.p3.D4_Con_tmax_Point_Operation.restrict(new JInt._12000());
		p.prim.D23_Con_Checksum_Data_Used.restrict(JBool.FALSE);
		p.prim.D2_Con_tmax_PDI_Connection.restrict(new JInt._20000());
		p.prim.D3_Con_PDI_Version.restrict(PDI_Version.V1);
		p.prim.D4_Con_Checksum_Data.restrict(PDI_Checksum_Data.D1);
		p.sec.D23_Con_Checksum_Data_Used.restrict(JBool.FALSE);
		p.sec.D3_Con_PDI_Version.restrict(PDI_Version.V1);
		p.sec.D4_Con_Checksum_Data.restrict(PDI_Checksum_Data.D1);
		fe.est.D20_Con_MDM_Used.restrict(JBool.FALSE);
		fe.smi.D1_Con_t_Ini_Def_Delay.restrict(new JInt._10000());
		fe.smi.D2_Con_t_Ini_Step.restrict(new JInt._10000());
		fe.smi.D3_Con_t_Ini_Max.restrict(new JInt._600000());
		fe.smi.D4_Con_tmax_Response_MDM.restrict(new JInt._1000());
		fe.smi.D5_Con_tmax_DataTransmission.restrict(new JInt._1000());
		p.sp.DT10_Move_Point.restrict(PointPos.LEFT, PointPos.RIGHT);
		
		//DO THIS BEFORE CREATING THE UNIFYING BLOCK:
		fe.est.T1_Power_On_Detected.getVerificationActions().add(vm.action("T1"));
		fe.est.T2_Power_Off_Detected.getVerificationActions().add(vm.action("T2"));
		fe.est.T21_Ready_For_PDI_Connection.getVerificationActions().add(vm.action("T21"));
		
		fe.sec.T5_SCP_Connection_Established.getVerificationActions().add(vm.action("T5"));
		fe.sec.T10_SCP_Connection_Terminated.getVerificationActions().add(vm.action("T10"));
		fe.sec.T20_Protocol_Error.getVerificationActions().add(vm.action("T10"));
		fe.sec.T21_Formal_Telegram_Error.getVerificationActions().add(vm.action("T10"));
		fe.sec.T22_Content_Telegram_Error.getVerificationActions().add(vm.action("T10"));
		fe.sec.D50_PDI_Connection_State.getVerificationActions().add(vm.action("D50"));
		
		
		UnifyingBlock ub = new UnifyingBlock("point", m, true, false);
		
////		new DecaFourExplorerGUI(ub.sms4);
////		new DecaFourSimulatorGUI(ub.sms4);
////		new MCRL2Printer(ub, new PrintingOptions()).printAndPop("models/point");
		
		PalmaGraph g1 = new PalmaGraph(ub, vm);
		System.out.println("palma-done");
//		new PalmaExporter(g1).saveToFile("palma.gv");
		NikolaGraph g2 = new NikolaGraph(g1);
//		new NikolaExporter(g2).saveToFile("nikola.gv");
		System.out.println("nikola-done");
		NecronGraph g3a = new NecronGraph(g2, false);
//		new NecronExporter(g3a).saveToFile("necron1.gv");
//		NecronGraph g3b = new NecronGraph(g2, true);
//		new NecronExporter(g3b).saveToFile("necron2.gv");
//		new NecronDiffExporter(g3a, g3b).saveToFile("necron-diff.gv");
		
		TeleGraph g4a = new TeleGraph(g3a);
//		new TeleExporter(g4a).saveToFile("tele1.gv");
		new TeleMcrl2Exporter(g4a).saveToFile("tele1.mcrl2");
		
		
//		TeleGraph g4b = new TeleGraph(g3b);
//		new TeleExporter(g4b).saveToFile("tele2.gv");
//		new TeleMcrl2Exporter(g4b).saveToFile("tele2.mcrl2");
		
//		JetGraph g5 = new JetGraph(g4a);
//		new JetAldebaranExporter(g5).saveToFile("jet.aut");
//		GateGraph g6 = new GateGraph(g5);
		
	}
}
