package fmics;

import java.io.IOException;

import jlx.asal.j.*;
import jlx.behave.stable.files.*;
import jlx.blocks.ibd1.*;
import jlx.common.reflection.ReflectionException;
import jlx.models.Model;
import jlx.models.UnifyingBlock;
import models.generic.types.*;
import models.generic.v3x0x0A.*;
import models.point.types.*;
import models.point.v2x7x0A.*;

public class Point_Compute_SIC_DFSM {
	public static void main(String[] args) throws ReflectionException, IOException {
		Model m = new Model();
		SCI_P_PDI_SR.Block p = m.add("pdi", new SCI_P_PDI_SR.Block()); //Must match name in EfeS_SCI_XX_Protocol_Stack_SR!
		SubS_P_SR.Block fe = m.add("fe", new SubS_P_SR.Block()); //Must match name in EfeS_SCI_XX_Functional_Connection_Domain_Context!
		EfeS_SCI_XX_Functional_Connection_Domain_Context.Block ctx = m.add("ctx", new EfeS_SCI_XX_Functional_Connection_Domain_Context.Block()); //Any name is fine.
		
		//We do not use the SCP components:
//		EfeS_SCI_XX_Protocol_Stack_SR.Block ps = m.add("ps", new EfeS_SCI_XX_Protocol_Stack_SR.Block()); //Any name is fine.
		//But we still need to connect via the SCI-XX interface:
		EfeS_SCI_XX_PDI_SR.Block pdi = m.add("pdi", new EfeS_SCI_XX_PDI_SR.Block());
		
		m.add("sp", new SCI_P_STD_1());
		m.add("prim", new SCI_EfeS_Prim_STD_1());
		m.add("sec", new SCI_EfeS_Sec_STD_1());
		m.add("est", new EST_EfeS_STD_2());
		m.add("fp", new SCI_P_STD_2());
		m.add("p3", new F_P3_SR_STD_1());
		
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
		fe.p3.D4_Con_tmax_Point_Operation.setExecutionTime(12000);
		p.prim.D23_Con_Checksum_Data_Used.restrict(JBool.FALSE);
		p.prim.D2_Con_tmax_PDI_Connection.restrict(new JInt._20000());
		p.prim.D2_Con_tmax_PDI_Connection.setExecutionTime(20000);
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
		
		//===== Adapter labels per simulator tab =====//
		AdapterLabels labels = new AdapterLabels();
		
		//Configuration:
		fe.p3.D30_Con_007000.set(labels.get("D30_0070001"));
		fe.p3.D32_Con_007600.set(labels.get("D32_0076001"));
		fe.p3.D33_Con_007900.set(labels.get("D33_0079001"));
		fe.p3.D34_Con_008000.set(labels.get("D34_0080001"));
		fe.p3.D35_Con_008200.set(labels.get("D35_0082001"));
		fe.p3.D36_Con_008300.set(labels.get("D36_0083001"));
		fe.p3.D37_Con_008400.set(labels.get("D37_0084001"));
		fe.p3.D38_Con_008500.set(labels.get("D38_0085001"));
		
		//Input SMI Field Element:
		fe.est.D20_Con_MDM_Used.set(labels.get("D20_Con_MDM_Used1"));
		fe.smi.D1_Con_t_Ini_Def_Delay.set(labels.get("D1_Con_t_Ini_Def_Delay1"));
		fe.smi.D2_Con_t_Ini_Step.set(labels.get("D2_Con_t_Ini_Step1"));
		fe.smi.D3_Con_t_Ini_Max.set(labels.get("D3_Con_t_Ini_Max1"));
		fe.smi.D4_Con_tmax_Response_MDM.set(labels.get("D4_Con_tmax_Response_MDM1"));
		fe.smi.D5_Con_tmax_DataTransmission.set(labels.get("D5_Con_tmax_DataTransmission1"));
		fe.smi.T6_Data_Up_To_Date.set(labels.get("T6_Data_Up_To_Date1"));
		fe.smi.T7_Data_Not_Up_To_Date.set(labels.get("T7_Data_Not_Up_To_Date1"));
		fe.smi.T8_Data.set(labels.get("T8_Data1"));
		fe.smi.T9_Transmission_Complete.set(labels.get("T9_Transmission_Complete1"));
		fe.smi.T10_Data_Valid.set(labels.get("T10_Data_Valid1"));
		fe.smi.T11_Data_Invalid.set(labels.get("T11_Data_Invalid1"));
		fe.smi.T12_Data_Installation_Successfully.set(labels.get("T12_Data_Installation_Successfully1"));
		
		//Input EST Field Element | Input SCI:
		fe.est.T1_Power_On_Detected.set(labels.get("T1_Power_On_Detected1"));
		fe.est.T2_Power_Off_Detected.set(labels.get("T2_Power_Off_Detected1"));
		fe.est.T3_Reset.set(labels.get("T3_Reset1"));
		fe.est.T4_Booted.set(labels.get("T4_Booted1"));
		fe.est.T5_SIL_Not_Fulfilled.set(labels.get("T5_SIL_Not_Fulfilled1"));
		fe.est.T7_Invalid_Or_Missing_Basic_Data.set(labels.get("T7_Invalid_Or_Missing_Basic_Data1"));
		p.prim.T5_SCP_Connection_Established.set(labels.get("T5_SCP_Connection1", 2));
		p.sec.T5_SCP_Connection_Established.set(labels.get("T5_SCP_Connection1", 2));
		p.prim.T10_SCP_Connection_Terminated.set(labels.get("T10_SCP_Connection_Terminated1", 2));
		p.sec.T10_SCP_Connection_Terminated.set(labels.get("T10_SCP_Connection_Terminated1", 2));
		p.prim.D2_Con_tmax_PDI_Connection.set(labels.get("D2_Con_tmax_PDI_Connection_I1"));
		p.prim.D3_Con_PDI_Version.set(labels.get("D3_Con_PDI_Version_I1"));
		p.prim.D4_Con_Checksum_Data.set(labels.get("D4_Con_Checksum_Data_I1"));
		p.prim.D23_Con_Checksum_Data_Used.set(labels.get("D23_Con_Checksum_Data_Used1", 2));
		p.sec.D3_Con_PDI_Version.set(labels.get("D3_Con_PDI_Version1"));
		p.sec.D4_Con_Checksum_Data.set(labels.get("D4_Checksum_Data1"));
		p.sec.D23_Con_Checksum_Data_Used.set(labels.get("D23_Con_Checksum_Data_Used1", 2));
		p.prim.T20_Protocol_Error.set(labels.get("T20_Protocol_Error_I1"));
		p.prim.T21_Formal_Telegram_Error.set(labels.get("T21_Formal_Telegram_Error_I1"));
		p.prim.T22_Content_Telegram_Error.set(labels.get("T22_Content_Telegram_Error_I1"));
		p.sec.T20_Protocol_Error.set(labels.get("T20_Protocol_Error1"));
		p.sec.T21_Formal_Telegram_Error.set(labels.get("T21_Formal_Telegram_Error1"));
		p.sec.T22_Content_Telegram_Error.set(labels.get("T22_Content_Telegram_Error1"));
		
		//SubS P Inputs:
		fe.p3.D4_Con_tmax_Point_Operation.set(labels.get("D04_Con_tmax_Point_Operation2"));
		p.sp.T10_Move_Point.set(labels.get("T01_Cmd_Move_Point2"));
		p.sp.DT10_Move_Point.set(labels.get("DT01_Move_Point_Target2"));
		fe.p3.D21_PM1_Position.set(labels.get("PM1_Position1"));
		fe.p3.D13_PM2_Activation.set(labels.get("D13_Active_PM21"));
		fe.p3.D22_PM2_Position.set(labels.get("PM2_Position1"));
		
		//SubS P Outputs:
		p.sp.T20_Point_Position.set(labels.get("T02_Msg_Point_Position1")); //Used to be T2_Msg_Point_Position1
		p.sp.DT20_Point_Position.set(labels.get("DT20_Position1")); //Used to be DT2_Point_Position1
		p.sp.T30_Timeout.set(labels.get("T3_Msg_Timeout1"));
		fe.p3.T4_Information_No_End_Position.set(labels.get("T04_Information_No_End_Position1"));
		fe.p3.D10_Move_Left.set(labels.get("D5_Move_Left1"));
		fe.p3.T5_Info_End_Position_Arrived.set(labels.get("T5_Info_End_Position_Arrived1"));
		fe.p3.D11_Move_Right.set(labels.get("D6_Move_Right1"));
		fe.p3.T6_Information_Trailed_Point.set(labels.get("T06_Information_Trailed_Point1"));
		fe.p3.D5_Drive_State.set(labels.get("D05_Drive_State1"));
		fe.p3.T7_Information_Out_Of_Sequence.set(labels.get("T07_Information_Out_Of_Sequence1"));
		fe.p3.D6_Detection_State.set(labels.get("D06_Detection_State1"));
		fe.p3.D25_Redrive.set(labels.get("D25_Redrive1"));
		
		//Generic:
		fe.smi.T19_Validate_Data.set(labels.get("T19_Validate_Data1"));
		fe.smi.T20_Ready_For_Update_Of_Data.set(labels.get("T20_Ready_For_Update_Of_Data1"));
		//internal T21_Data_Update_Finished1
		p.sec.T12_Terminate_SCP_Connection.set(labels.get("T12_Disconnect_SCP1"));
		//internal D51_F_EST_EfeS_Gen_SR_state1
		//internal T13_Msg_PDI_Version_Check1
		//internal D50_PDI_Connection_State_S1
		//internal T14_Msg_Start_Initialisation1
		//internal DT13_Result1
		//internal T15_Msg_Initialisation_Completed1
		//internal DT13_Checksum_data1
		//internal T18_Start_Status_Report1
		p.prim.T6_Establish_SCP_Connection.set(labels.get("T6_Establish_SCP_Connection_I1"));
		//internal D50_PDI_Connection_State1
		//internal T7_Cd_PDI_Version_Check11
		//internal DT7_PDI_Version11
		//internal T8_Cd_Initialisation_Request12
		p.prim.T12_Terminate_SCP_Connection.set(labels.get("T12_Terminate_SCP_Connection_I1"));
		
		long prevTime = System.currentTimeMillis();
		
		UnifyingBlock ub = new UnifyingBlock("point", m, false, true);
		new DecaStableFileBuilder(ub.stableSm).saveToFile("output", "point.sic");
		
		System.out.println("#time-elapsed = " + (0.001f * (System.currentTimeMillis() - prevTime)) + "s");
	}
}

