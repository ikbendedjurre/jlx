package models.lc.v1x0;

import jlx.asal.j.*;

public class Local_Operation_Handover extends JUserType<Local_Operation_Handover> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Local_Operation_Handover {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Handover to local operator initiated")
	public final static class Handover_to_local_operator_initiated extends Local_Operation_Handover {}
	public final static Handover_to_local_operator_initiated Handover_to_local_operator_initiated = new Handover_to_local_operator_initiated();
	@JTypeName(s = "Handover to local operator established")
	public final static class Handover_to_local_operator_established extends Local_Operation_Handover {}
	public final static Handover_to_local_operator_established Handover_to_local_operator_established = new Handover_to_local_operator_established();
	@JTypeName(s = "Handover to local operator returned")
	public final static class Handover_to_local_operator_returned extends Local_Operation_Handover {}
	public final static Handover_to_local_operator_returned Handover_to_local_operator_returned = new Handover_to_local_operator_returned();
}
