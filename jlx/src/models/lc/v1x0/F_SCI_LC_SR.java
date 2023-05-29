package models.lc.v1x0;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 30/47
 */
public class F_SCI_LC_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_Init = new Initialization(
			"T101_Realise_Activation := FALSE;\n" + 
			"DT101_Realise_Activation := \"Cd_Activation.undefined\";\n" + 
			"T102_Realise_Deactivation := FALSE;\n" + 
			"T103_Realise_Local_Operation_Handover := FALSE;\n" + 
			"DT103_Realise_Local_Operation_Handover := \"Local_Operation_Handover.undefined\";\n" + 
			"T104_Realise_Isolate_LC := FALSE;\n" + 
			"DT104_Realise_Isolate_LC := \"Cd_Isolate_LC.undefined\";\n" + 
			"T5_Msg_LC_Functional_Status := FALSE;\n" + 
			"DT5_Msg_LC_Functional_Status := \"Functional_Status.undefined\";\n" + 
			"T6_Msg_LC_Monitoring_Status := FALSE;\n" + 
			"DT6_Msg_LC_Monitoring_Status := \"Monitoring_Status.undefined\";\n" + 
			"T7_Msg_LC_Failure_Status := FALSE;\n" + 
			"DT7_Msg_LC_Failure_Status := \"Failure_Status.undefined\";\n" + 
			"T8_Msg_Detection_Element_Status := FALSE;\n" + 
			"DT8_Msg_Detection_Element_Status := \"Detection_Element_Status.undefined\";\n" + 
			"T9_Msg_Obstacle_Detection_Status := FALSE;\n" + 
			"DT9_Msg_Obstacle_Detection_Status := \"Obstacle_Detection_Status.undefined\";\n" + 
			"T10_Msg_Local_Operation_Handover := FALSE;\n" + 
			"DT10_Msg_Local_Operation_Handover := \"Msg_Local_Operation_Handover.undefined\";\n" + 
			"T11_Msg_Local_Request := FALSE;\n" + 
			"DT11_Msg_Local_Request := \"Local_Request.undefined\";\n" + 
			"T52_All_Status_send := FALSE;"
		);
		
		public final InPort<JPulse> T4_Cd_Isolate_LC = new InPort<>();
		public final InPort<PDI_Connection_State> D50_PDI_Connection_State = new InPort<>(); //String
		public final InPort<Functional_Status> DT105_Report_LC_Functional_Status = new InPort<>(); //String
		public final InPort<Monitoring_Status> DT106_Report_LC_Monitoring_Status = new InPort<>(); //String
		public final InPort<Failure_Status> DT107_Report_LC_Failure_Status = new InPort<>(); //String
		public final InPort<Msg_Local_Operation_Handover> DT110_Report_Local_Operation_Handover = new InPort<>(); //String
		public final InPort<Cd_Activation> DT1_Cd_Activation = new InPort<>(); //String
		public final InPort<JPulse> T105_Report_LC_Functional_Status = new InPort<>();
		public final InPort<JPulse> T106_Report_LC_Monitoring_Status = new InPort<>();
		public final InPort<JPulse> T107_Report_LC_Failure_Status = new InPort<>();
		public final InPort<JPulse> T110_Report_Local_Operation_Handover = new InPort<>();
		public final InPort<JPulse> T1_Cd_Activation = new InPort<>();
		public final InPort<JPulse> T2_Cd_Deactivation = new InPort<>();
		public final InPort<JPulse> T3_Cd_Local_Operation_Handover = new InPort<>();
		public final InPort<Local_Operation_Handover> DT3_Cd_Local_Operation_Handover = new InPort<>(); //String
		public final InPort<Cd_Isolate_LC> DT4_Cd_Isolate_LC = new InPort<>(); //String
		public final InPort<Obstacle_Detection_Status> DT109_Report_Obstacle_Detection_Status = new InPort<>(); //String
		public final InPort<JPulse> T108_Report_Detection_Element_Status = new InPort<>();
		public final InPort<JPulse> T109_Report_Obstacle_Detection_Status = new InPort<>();
		public final InPort<Local_Request> DT111_Report_Local_Request = new InPort<>(); //String
		public final InPort<JPulse> T111_Report_Local_Request = new InPort<>();
		public final InPort<Detection_Element_Status> DT108_Report_Detection_Element_Status = new InPort<>(); //String
		public final InPort<JPulse> T199_All_Status_Send = new InPort<>();
		
		public final OutPort<Cd_Activation> DT101_Realise_Activation = new OutPort<>(); //String
		public final OutPort<Cd_Isolate_LC> DT104_Realise_Isolate_LC = new OutPort<>(); //String
		public final OutPort<Functional_Status> DT5_Msg_LC_Functional_Status = new OutPort<>(); //String
		public final OutPort<Monitoring_Status> DT6_Msg_LC_Monitoring_Status = new OutPort<>(); //String
		public final OutPort<Failure_Status> DT7_Msg_LC_Failure_Status = new OutPort<>(); //String
		public final OutPort<Msg_Local_Operation_Handover> DT10_Msg_Local_Operation_Handover = new OutPort<>(); //String
		public final OutPort<JPulse> T101_Realise_Activation = new OutPort<>();
		public final OutPort<JPulse> T102_Realise_Deactivation = new OutPort<>();
		public final OutPort<JPulse> T103_Realise_Local_Operation_Handover = new OutPort<>();
		public final OutPort<JPulse> T104_Realise_Isolate_LC = new OutPort<>();
		public final OutPort<JPulse> T5_Msg_LC_Functional_Status = new OutPort<>();
		public final OutPort<JPulse> T6_Msg_LC_Monitoring_Status = new OutPort<>();
		public final OutPort<JPulse> T7_Msg_LC_Failure_Status = new OutPort<>();
		public final OutPort<JPulse> T10_Msg_Local_Operation_Handover = new OutPort<>();
		public final OutPort<Local_Operation_Handover> DT103_Realise_Local_Operation_Handover = new OutPort<>(); //String
		public final OutPort<Detection_Element_Status> DT8_Msg_Detection_Element_Status = new OutPort<>(); //String
		public final OutPort<Obstacle_Detection_Status> DT9_Msg_Obstacle_Detection_Status = new OutPort<>(); //String
		public final OutPort<JPulse> T8_Msg_Detection_Element_Status = new OutPort<>();
		public final OutPort<JPulse> T9_Msg_Obstacle_Detection_Status = new OutPort<>();
		public final OutPort<Local_Request> DT11_Msg_Local_Request = new OutPort<>(); //String
		public final OutPort<JPulse> T11_Msg_Local_Request = new OutPort<>();
		public final OutPort<JPulse> T52_All_Status_send = new OutPort<>();
	}
}
