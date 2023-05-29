package models.lc.v1x0;

import jlx.asal.j.*;


public class Failure_Status extends JUserType<Failure_Status> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Failure_Status {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "A non critical failure is present")
	public final static class A_non_critical_failure_is_present extends Failure_Status {}
	public final static A_non_critical_failure_is_present A_non_critical_failure_is_present = new A_non_critical_failure_is_present();
	@JTypeName(s = "A critical failure is present")
	public final static class A_critical_failure_is_present extends Failure_Status {}
	public final static A_critical_failure_is_present A_critical_failure_is_present = new A_critical_failure_is_present();
	@JTypeName(s = "No failure present")
	public final static class No_failure_present extends Failure_Status {}
	public final static No_failure_present No_failure_present = new No_failure_present();
	@JTypeName(s = "Current Failure status")
	public final static class Current_Failure_status extends Failure_Status {}
	public final static Current_Failure_status Current_Failure_status = new Current_Failure_status();
	@JTypeName(s = "Failure detected")
	public final static class Failure_detected extends Failure_Status {}
	public final static Failure_detected Failure_detected = new Failure_detected();
}
