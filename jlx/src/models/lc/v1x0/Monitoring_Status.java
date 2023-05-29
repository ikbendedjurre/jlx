package models.lc.v1x0;

import jlx.asal.j.*;

public class Monitoring_Status extends JUserType<Monitoring_Status> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Monitoring_Status {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Closure timer overrun occurred")
	public final static class Closure_timer_overrun_occurred extends Monitoring_Status {}
	public final static Closure_timer_overrun_occurred Closure_timer_overrun_occurred = new Closure_timer_overrun_occurred();
	@JTypeName(s = "No Closure timer overrun")
	public final static class No_Closure_timer_overrun extends Monitoring_Status {}
	public final static No_Closure_timer_overrun No_Closure_timer_overrun = new No_Closure_timer_overrun();
	@JTypeName(s = "Changed Monitoring Parameter")
	public final static class Changed_Monitoring_Parameter extends Monitoring_Status {}
	public final static Changed_Monitoring_Parameter Changed_Monitoring_Parameter = new Changed_Monitoring_Parameter();
	@JTypeName(s = "Current Monitoring status")
	public final static class Current_Monitoring_status extends Monitoring_Status {}
	public final static Current_Monitoring_status Current_Monitoring_status = new Current_Monitoring_status();
}