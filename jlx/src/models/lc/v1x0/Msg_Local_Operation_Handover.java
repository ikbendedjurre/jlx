package models.lc.v1x0;

import jlx.asal.j.*;

public class Msg_Local_Operation_Handover extends JUserType<Msg_Local_Operation_Handover> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Msg_Local_Operation_Handover {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Allow handover from local operator")
	public final static class Allow_handover_from_local_operator extends Msg_Local_Operation_Handover {}
	public final static Allow_handover_from_local_operator Allow_handover_from_local_operator = new Allow_handover_from_local_operator();
	
	@JTypeName(s = "Return handover from local operator")
	public final static class Return_handover_from_local_operator extends Msg_Local_Operation_Handover {}
	public final static Return_handover_from_local_operator Return_handover_from_local_operator = new Return_handover_from_local_operator();
}
