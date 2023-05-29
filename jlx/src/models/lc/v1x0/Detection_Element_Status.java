package models.lc.v1x0;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class Detection_Element_Status extends JUserType<Detection_Element_Status> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Detection_Element_Status {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Current Detection Element Status")
	public final static class Current_Detection_Element_Status extends Detection_Element_Status {}
	public final static Current_Detection_Element_Status Current_Detection_Element_Status = new Current_Detection_Element_Status();
}
