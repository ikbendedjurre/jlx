package models.lc.v1x0;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class Local_Request extends JUserType<Local_Request> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Local_Request {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Local request to activate the level crossing")
	public final static class Local_request_to_activate_the_level_crossing extends Local_Request {}
	public final static Local_request_to_activate_the_level_crossing Local_request_to_activate_the_level_crossing = new Local_request_to_activate_the_level_crossing();
	@JTypeName(s = "Local request to deactivate the level crossing")
	public final static class Local_request_to_deactivate_the_level_crossing extends Local_Request {}
	public final static Local_request_to_deactivate_the_level_crossing Local_request_to_deactivate_the_level_crossing = new Local_request_to_deactivate_the_level_crossing();

}
