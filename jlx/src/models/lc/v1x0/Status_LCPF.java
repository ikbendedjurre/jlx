package models.lc.v1x0;

import jlx.asal.j.*;

public class Status_LCPF extends JUserType<Status_LCPF> {
	@JTypeName(s = "Changed Monitoring Parameter")
	public final static class Changed_Monitoring_Parameter extends Status_LCPF {}
	public final static Changed_Monitoring_Parameter Changed_Monitoring_Parameter = new Changed_Monitoring_Parameter();
	@JTypeName(s = "No obstacle in the conflict area")
	public final static class No_obstacle_in_the_conflict_area extends Status_LCPF {}
	public final static No_obstacle_in_the_conflict_area No_obstacle_in_the_conflict_area = new No_obstacle_in_the_conflict_area();
	@JTypeName(s = "Obstacle detected in the conflict area")
	public final static class Obstacle_detected_in_the_conflict_area extends Status_LCPF {}
	public final static Obstacle_detected_in_the_conflict_area Obstacle_detected_in_the_conflict_area = new Obstacle_detected_in_the_conflict_area();
	@JTypeName(s = "Protected")
	public final static class Protected extends Status_LCPF {}
	public final static Protected Protected = new Protected();
	@JTypeName(s = "Unprotected")
	public final static class Unprotected extends Status_LCPF {}
	public final static Unprotected Unprotected = new Unprotected();
	@JTypeName(s = "Idle")
	public final static class Idle extends Status_LCPF {}
	public final static Idle Idle = new Idle();
	@JTypeName(s = "Failure detected")
	public final static class Failure_detected extends Status_LCPF {}
	public final static Failure_detected Failure_detected = new Failure_detected();
	@JTypeName(s = "No failure present")
	public final static class No_failure_present extends Status_LCPF {}
	public final static No_failure_present No_failure_present = new No_failure_present();
}