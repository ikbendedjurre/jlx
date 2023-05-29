package models.lc.v1x0;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 34/47
 */
public class F_LC_Functions_SR {
	public static class Block extends Type1IBD {
		public final JBool Mem_Closure_Timer_Expired = new JBool();
		public final JBool Mem_Closure_Timer_Running = new JBool();
		public final Functional_Status Mem_Last_LC_State = new Functional_Status(); //String
		
		public final Initialization cOp1_Init = new Initialization(
			"T5_Msg_LC_Functional_Status := FALSE;\n" + 
			"DT5_Msg_LC_Functional_Status := \"Functional_Status.undefined\";\n" + 
			"T6_Msg_LC_Monitoring_Status := FALSE;\n" + 
			"DT6_Msg_LC_Monitoring_Status := \"Monitoring_Status.undefined\";\n" + 
			"T7_Msg_LC_Failure_Status := FALSE;\n" + 
			"DT7_Msg_LC_Failure_Status := \"Failure_Status.undefined\";\n" + 
			"T8_Msg_Local_Request := FALSE;\n" + 
			"DT8_Msg_Local_Request := \"Local_Request.undefined\";\n" + 
			"T9_Msg_Local_Operation_Handover := FALSE;\n" + 
			"DT9_Msg_Local_Operation_Handover := \"Msg_Local_Operation_Handover.undefined\";\n" + 
			"T31_Activate_LCPF := FALSE;\n" + 
			"T32_Deactivate_LCPF := FALSE;\n" + 
			"T33_Pre_Activate_LCPF := FALSE;\n" + 
			"T34_National_Specific_State_LCPF := FALSE;\n" + 
			"T42_Output_Initiated_Handover_To_Local_Operator := FALSE;\n" + 
			"T43_Output_Established_Handover_To_Local_Operator := FALSE;\n" + 
			"T44_Output_No_Handover_To_Local_Operator := FALSE;\n" + 
			"T18_Msg_Detection_Element_Status := FALSE;\n" + 
			"DT18_Msg_Detection_Element_Status := \"Detection_Element_Status.undefined\";\n" + 
			"T91_Msg_Obstacle_Detection_Status := FALSE;\n" + 
			"DT91_Msg_Obstacle_Detection_Status := \"Obstacle_Detection_Status.undefined\";"
		);
		
		public final Operation<JVoid> cOp2_React_On_Closure_Timer_Overrun = new Operation<JVoid>(
				"if D68_Failure_Status_After_Closure_Timer_Overrun = \"non critical failure report\" then\n" + 
				"	DT6_Msg_LC_Monitoring_Status := \"Closure timer overrun occurred\";\n" + 
				"	T6_Msg_LC_Monitoring_Status := TRUE;\n" +
				"	DT7_Msg_LC_Failure_Status := \"A non critical failure is present\";\n" + 
				"	T7_Msg_LC_Failure_Status := TRUE;\n" + 
				"elseif D68_Failure_Status_After_Closure_Timer_Overrun = \"critical failure report\" then\n" + 
				"	DT6_Msg_LC_Monitoring_Status := \"Closure timer overrun occurred\";\n" + 
				"	T6_Msg_LC_Monitoring_Status := TRUE;\n" + 
				"	DT7_Msg_LC_Failure_Status := \"A critical failure is present\";\n" + 
				"	T7_Msg_LC_Failure_Status := TRUE;\n" + 
				"else\n" + 
				"	DT6_Msg_LC_Monitoring_Status := \"Closure timer overrun occurred\";\n" + 
				"	T6_Msg_LC_Monitoring_Status := TRUE;\n" + 
				"end if"
				);
		
		public final Operation<JVoid> cOp3_React_On_No_Closure_Timer_Overrun = new Operation<JVoid>(
				"if D68_Failure_Status_After_Closure_Timer_Overrun = \"non critical failure report\" then\n" + 
				"	DT6_Msg_LC_Monitoring_Status := \"No Closure timer overrun\";\n" + 
				"	T6_Msg_LC_Monitoring_Status := TRUE;\n" + 
				"	DT7_Msg_LC_Failure_Status := \"Failure_Status.No_failure_present\";\n" + 
				"	T7_Msg_LC_Failure_Status := TRUE;\n" + 
				"elseif D68_Failure_Status_After_Closure_Timer_Overrun = \"critical failure report\" then\n" + 
				"	DT6_Msg_LC_Monitoring_Status := \"No Closure timer overrun\";\n" + 
				"	T6_Msg_LC_Monitoring_Status := TRUE;\n" + 
				"	DT7_Msg_LC_Failure_Status := \"Failure_Status.No_failure_present\";\n" + 
				"	T7_Msg_LC_Failure_Status := TRUE;\n" + 
				"else\n" + 
				"	DT6_Msg_LC_Monitoring_Status := \"No Closure timer overrun\";\n" + 
				"	T6_Msg_LC_Monitoring_Status := TRUE;\n" + 
				"end if"
				);
		
		public final InPort<Cd_Activation> DT1_Cd_Activation = new InPort<>();	//String
		public final InPort<Cd_Isolate_LC> DT4_Cd_Isolate_LC = new InPort<>();	//String
		public final InPort<Local_Operation_Handover> DT3_Cd_Local_Operation_Handover = new InPort<>(); //String	
		public final InPort<Status_LCPF> DT30_Status_LCPF = new InPort<>(); //String
		public final InPort<JInt> D62_Con_t_PDI_Con_Loss_Deactivation_Timer = new InPort<>();		
		public final InPort<JInt> D61_Con_tmax_Closure_Timer = new InPort<>();		
		public final InPort<JBool> D63_Con_Use_Closure_Timer = new InPort<>();		
		public final InPort<JBool> D64_Con_Use_PDI_Con_Loss_Deactivation_Timer = new InPort<>();		
		public final InPort<JBool> D65_Con_Use_Pre_Activation = new InPort<>();		
		public final InPort<JBool> D60_LC_Failure = new InPort<>();		
		public final InPort<EST_EfeS_State> D50_EST_EfeS_State = new InPort<>(); //String	
		public final InPort<JPulse> T1_Cd_Activation = new InPort<>();		
		public final InPort<JPulse> T2_Cd_Deactivation = new InPort<>();		
		public final InPort<JPulse> T4_Cd_Isolate_LC = new InPort<>();		
		public final InPort<JPulse> T40_Activate_By_Local_Operator = new InPort<>();		
		public final InPort<JPulse> T3_Cd_Local_Operation_Handover = new InPort<>();		
		public final InPort<JPulse> T41_Deactivate_By_Local_Operator = new InPort<>();		
		public final InPort<JPulse> T45_Input_Allow_Handover_To_Local_Operator = new InPort<>();		
		public final InPort<JPulse> T46_Input_Return_Handover_To_Local_Operator = new InPort<>();		
		public final InPort<JPulse> T49_Report_Status = new InPort<>();		
		public final InPort<JPulse> T30_Status_LCPF = new InPort<>();		
		public final InPort<JBool> D108_Con_Use_Detection_Element = new InPort<>();		
		public final InPort<JBool> D66_Con_Use_Obstacle_Detection = new InPort<>();		
		public final InPort<JBool> D67_Con_Use_Isolation = new InPort<>();		
		public final InPort<Failure_Report> D68_Failure_Status_After_Closure_Timer_Overrun = new InPort<>(); //String		
		public final InPort<Detection_Element_Status> DT108_Detection_Element_Status = new InPort<>(); //String	
		public final InPort<JPulse> T108_Detection_Element_Status = new InPort<>();		
		
		public final OutPort<Functional_Status> DT5_Msg_LC_Functional_Status = new OutPort<>(); //String		
		public final OutPort<Failure_Status> DT7_Msg_LC_Failure_Status = new OutPort<>(); //String	
		public final OutPort<Monitoring_Status> DT6_Msg_LC_Monitoring_Status = new OutPort<>(); //String		
		public final OutPort<Msg_Local_Operation_Handover> DT9_Msg_Local_Operation_Handover = new OutPort<>(); //String		
		public final OutPort<Local_Request> DT8_Msg_Local_Request = new OutPort<>(); //String	
		public final OutPort<JPulse> T5_Msg_LC_Functional_Status = new OutPort<>();		
		public final OutPort<JPulse> T7_Msg_LC_Failure_Status = new OutPort<>();		
		public final OutPort<JPulse> T6_Msg_LC_Monitoring_Status = new OutPort<>();		
		public final OutPort<JPulse> T9_Msg_Local_Operation_Handover = new OutPort<>();		
		public final OutPort<JPulse> T8_Msg_Local_Request = new OutPort<>();		
		public final OutPort<JPulse> T43_Output_Established_Handover_To_Local_Operator = new OutPort<>();		
		public final OutPort<JPulse> T42_Output_Initiated_Handover_To_Local_Operator = new OutPort<>();		
		public final OutPort<JPulse> T44_Output_No_Handover_To_Local_Operator = new OutPort<>();		
		public final OutPort<JPulse> T31_Activate_LCPF = new OutPort<>();		
		public final OutPort<JPulse> T32_Deactivate_LCPF = new OutPort<>();		
		public final OutPort<JPulse> T34_National_Specific_State_LCPF = new OutPort<>();		
		public final OutPort<JPulse> T33_Pre_Activate_LCPF = new OutPort<>();		
		public final OutPort<Detection_Element_Status> DT18_Msg_Detection_Element_Status = new OutPort<>(); //String		
		public final OutPort<Obstacle_Detection_Status> DT91_Msg_Obstacle_Detection_Status = new OutPort<>(); //String		
		public final OutPort<JPulse> T18_Msg_Detection_Element_Status = new OutPort<>();		
		public final OutPort<JPulse> T91_Msg_Obstacle_Detection_Status = new OutPort<>();		
		public final OutPort<JPulse> T99_Msg_All_Status_Send = new OutPort<>();	
	}
}
