package models.lc.v1x0;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;
import models.generic.types.*;

/**
 * Requirements specification SCI-LC Eu.Doc.108 v1.0
 * Page 26/47
 */
public class S_SCI_LC_SR {
	public static class Block extends Type1IBD {
		public final Initialization cOp1_Init = new Initialization(
			"T101_Cd_Activation := FALSE;\n" + 
			"DT101_Cd_Activation := \"Cd_Activation.undefined\";\n" + 
			"T102_Cd_Deactivation := FALSE;\n" + 
			"T103_Cd_Local_Operation_Handover := FALSE;\n" + 
			"DT103_Cd_Local_Operation_Handover := \"Local_Operation_Handover.undefined\";\n" + 
			"T104_Cd_Isolate_LC := FALSE;\n" + 
			"DT104_Cd_Isolate_LC := \"Cd_Isolate_LC.undefined\";\n" + 
			"T5_Report_LC_Functional_Status := FALSE;\n" + 
			"DT5_Report_LC_Functional_Status := \"Functional_Status.undefined\";\n" + 
			"T6_Report_LC_Monitoring_Status := FALSE;\n" + 
			"DT6_Report_LC_Monitoring_Status := \"Monitoring_Status.undefined\";\n" + 
			"T7_Report_LC_Failure_Status := FALSE;\n" + 
			"DT7_Report_LC_Failure_Status := \"Failure_Status.undefined\";\n" + 
			"T8_Report_Detection_Element_Status := FALSE;\n" + 
			"DT8_Report_Detection_Element_Status := \"Detection_Element_Status.undefined\";\n" + 
			"T9_Report_Obstacle_Detection_Status := FALSE;\n" + 
			"DT9_Report_Obstacle_Detection_Status := \"Obstacle_Detection_Status.undefined\";\n" + 
			"T10_Report_Local_Operation_Handover := FALSE;\n" + 
			"DT10_Report_Local_Operation_Handover := \"Msg_Local_Operation_Handover.undefined\";\n" + 
			"T11_Report_Local_Request := TRUE;\n" + 
			"DT11_Report_Local_Request := \"Local_Request.undefined\";"
		);
		
		//left column
		public final InPort<JPulse> T1_Realise_Activation = new InPort<>();
		public final InPort<Cd_Activation> DT1_Realise_Activation = new InPort<>(); //String
		public final InPort<JPulse> T2_Realise_Deactivation = new InPort<>();
		public final InPort<JPulse> T3_Realise_Local_Operation_Handover = new InPort<>();
		public final InPort<Local_Operation_Handover> DT3_Realise_Local_Operation_Handover = new InPort<>(); //String
		public final InPort<JPulse> T4_Realise_Isolate_LC = new InPort<>();
		public final InPort<Cd_Isolate_LC> DT4_Realise_Isolate_LC = new InPort<>(); //String
		public final InPort<PDI_Connection_State> D50_PDI_Connection_State = new InPort<>(); //String
		public final InPort<JPulse> T105_Msg_LC_Functional_Status = new InPort<>();
		public final InPort<Functional_Status> DT105_Msg_LC_Functional_Status = new InPort<>(); //String
		public final InPort<JPulse> T106_Msg_LC_Monitoring_Status = new InPort<>();
		public final InPort<Monitoring_Status> DT106_Msg_LC_Monitoring_Status = new InPort<>(); //String
		public final InPort<JPulse> T107_Msg_LC_Failure_Status = new InPort<>();
		public final InPort<Failure_Status> DT107_Msg_LC_Failure_Status = new InPort<>(); //String
		public final InPort<JPulse> T108_Msg_Detection_Element_Status = new InPort<>();
		public final InPort<Detection_Element_Status> DT108_Msg_Detection_Element_Status = new InPort<>(); //String
		public final InPort<JPulse> T109_Msg_Obstacle_Detection_Status = new InPort<>();
		public final InPort<Obstacle_Detection_Status> DT109_Msg_Obstacle_Detection_Status = new InPort<>(); //String
		public final InPort<JPulse> T110_Msg_Local_Operation_Handover = new InPort<>();
		public final InPort<Msg_Local_Operation_Handover> DT110_Msg_Local_Operation_Handover = new InPort<>(); //String
		public final InPort<JPulse> T111_Msg_Local_Request = new InPort<>();
		public final InPort<Local_Request> DT111_Msg_Local_Request = new InPort<>(); //String
		
		//right column		
		public final OutPort<Cd_Activation> DT101_Cd_Activation = new OutPort<>(); //String
		public final OutPort<Cd_Isolate_LC> DT104_Cd_Isolate_LC = new OutPort<>(); //String
		public final OutPort<Functional_Status> DT5_Report_LC_Functional_Status = new OutPort<>(); //String
		public final OutPort<Monitoring_Status> DT6_Report_LC_Monitoring_Status = new OutPort<>(); //String
		public final OutPort<Failure_Status> DT7_Report_LC_Failure_Status = new OutPort<>(); //String
		public final OutPort<Msg_Local_Operation_Handover> DT10_Report_Local_Operation_Handover = new OutPort<>(); //String
		public final OutPort<JPulse> T101_Cd_Activation = new OutPort<>();
		public final OutPort<JPulse> T102_Cd_Deactivation = new OutPort<>();
		public final OutPort<JPulse> T103_Cd_Local_Operation_Handover = new OutPort<>();
		public final OutPort<JPulse> T104_Cd_Isolate_LC = new OutPort<>();
		public final OutPort<JPulse> T5_Report_LC_Functional_Status = new OutPort<>();
		public final OutPort<JPulse> T6_Report_LC_Monitoring_Status = new OutPort<>();
		public final OutPort<JPulse> T7_Report_LC_Failure_Status = new OutPort<>();
		public final OutPort<JPulse> T10_Report_Local_Operation_Handover = new OutPort<>();
		public final OutPort<Local_Operation_Handover> DT103_Cd_Local_Operation_Handover = new OutPort<>(); //String
		public final OutPort<Detection_Element_Status> DT8_Report_Detection_Element_Status = new OutPort<>(); //String
		public final OutPort<Obstacle_Detection_Status> DT9_Report_Obstacle_Detection_Status = new OutPort<>(); //String
		public final OutPort<JPulse> T8_Report_Detection_Element_Status = new OutPort<>();
		public final OutPort<JPulse> T9_Report_Obstacle_Detection_Status = new OutPort<>();
		public final OutPort<Local_Request> DT11_Report_Local_Request = new OutPort<>(); //String
		public final OutPort<JPulse> T11_Report_Local_Request = new OutPort<>();
	}
}
