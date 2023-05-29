package models.lx;

import jlx.asal.j.*;

public class ExternalInputType extends JUserType<ExternalInputType> {
	@JTypeName(s = "")
	public final static class Empty extends ExternalInputType {}
	@JTypeName(s = "Msg_LX_Failure_Status")
	public final static class Msg_LX_Failure_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_LX_Functional_Status")
	public final static class Msg_LX_Functional_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_LX_Monitoring_Status")
	public final static class Msg_LX_Monitoring_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_Track_related_Failure_Status")
	public final static class Msg_Track_related_Failure_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_Track_related_Functional_Status")
	public final static class Msg_Track_related_Functional_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_Track_related_Monitoring_Status")
	public final static class Msg_Track_related_Monitoring_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_Detection_Element_Status")
	public final static class Msg_Detection_Element_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_LX_Command_Admissibility")
	public final static class Msg_LX_Command_Admissibility extends ExternalInputType {}
	@JTypeName(s = "Msg_Obstacle_Detection_Status")
	public final static class Msg_Obstacle_Detection_Status extends ExternalInputType {}
	@JTypeName(s = "Msg_Status_Of_Activation_Point")
	public final static class Msg_Status_Of_Activation_Point extends ExternalInputType {}
	@JTypeName(s = "Msg_Track_related_Command_Admissibility")
	public final static class Msg_Track_related_Command_Admissibility extends ExternalInputType {}
	
	@JTypeName(s = "Cd_Track_related_Activation")
	public final static class Cd_Track_related_Activation extends ExternalInputType {}
	@JTypeName(s = "Cd_Track_related_Deactivation")
	public final static class Cd_Track_related_Deactivation extends ExternalInputType {}
	@JTypeName(s = "Cd_Control_Activation_Point")
	public final static class Cd_Control_Activation_Point extends ExternalInputType {}
	@JTypeName(s = "Cd_LX_Activation")
	public final static class Cd_LX_Activation extends ExternalInputType {}
	@JTypeName(s = "Cd_LX_Deactivation")
	public final static class Cd_LX_Deactivation extends ExternalInputType {}
	@JTypeName(s = "Cd_Block_LX")
	public final static class Cd_Block_LX extends ExternalInputType {}
	@JTypeName(s = "Cd_Track_Related_Isolation")
	public final static class Cd_Track_Related_Isolation extends ExternalInputType {}
	@JTypeName(s = "Cd_Track_related_Prolong_Activation")
	public final static class Cd_Track_related_Prolong_Activation extends ExternalInputType {}
	@JTypeName(s = "Cd_Crossing_Clear")
	public final static class Cd_Crossing_Clear extends ExternalInputType {}
	
	public final static Empty Empty = new Empty();
	public final static Msg_LX_Failure_Status Msg_LX_Failure_Status = new Msg_LX_Failure_Status();
	public final static Msg_LX_Functional_Status Msg_LX_Functional_Status = new Msg_LX_Functional_Status();
	public final static Msg_LX_Monitoring_Status Msg_LX_Monitoring_Status = new Msg_LX_Monitoring_Status();
	public final static Msg_Track_related_Failure_Status Msg_Track_related_Failure_Status = new Msg_Track_related_Failure_Status();
	public final static Msg_Track_related_Functional_Status Msg_Track_related_Functional_Status = new Msg_Track_related_Functional_Status();
	public final static Msg_Track_related_Monitoring_Status Msg_Track_related_Monitoring_Status = new Msg_Track_related_Monitoring_Status();
	public final static Msg_Detection_Element_Status Msg_Detection_Element_Status = new Msg_Detection_Element_Status();
	public final static Msg_LX_Command_Admissibility Msg_LX_Command_Admissibility = new Msg_LX_Command_Admissibility();
	public final static Msg_Obstacle_Detection_Status Msg_Obstacle_Detection_Status = new Msg_Obstacle_Detection_Status();
	public final static Msg_Status_Of_Activation_Point Msg_Status_Of_Activation_Point = new Msg_Status_Of_Activation_Point();
	public final static Msg_Track_related_Command_Admissibility Msg_Track_related_Command_Admissibility = new Msg_Track_related_Command_Admissibility();
	
	public final static Cd_Track_related_Activation Cd_Track_related_Activation = new Cd_Track_related_Activation();
	public final static Cd_Track_related_Deactivation Cd_Track_related_Deactivation = new Cd_Track_related_Deactivation();
	public final static Cd_Control_Activation_Point Cd_Control_Activation_Point = new Cd_Control_Activation_Point();
	public final static Cd_LX_Activation Cd_LX_Activation = new Cd_LX_Activation();
	public final static Cd_LX_Deactivation Cd_LX_Deactivation = new Cd_LX_Deactivation();
	public final static Cd_Block_LX Cd_Block_LX = new Cd_Block_LX();
	public final static Cd_Track_Related_Isolation Cd_Track_Related_Isolation = new Cd_Track_Related_Isolation();
	public final static Cd_Track_related_Prolong_Activation Cd_Track_related_Prolong_Activation = new Cd_Track_related_Prolong_Activation();
	public final static Cd_Crossing_Clear Cd_Crossing_Clear = new Cd_Crossing_Clear();
}
