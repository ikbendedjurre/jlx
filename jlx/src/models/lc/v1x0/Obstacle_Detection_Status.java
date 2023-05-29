package models.lc.v1x0;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class Obstacle_Detection_Status extends JUserType<Obstacle_Detection_Status> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Obstacle_Detection_Status {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Current Obstacle Detection Status")
	public final static class Current_Obstacle_Detection_Status extends Obstacle_Detection_Status {}
	public final static Current_Obstacle_Detection_Status Current_Obstacle_Detection_Status = new Current_Obstacle_Detection_Status();
	@JTypeName(s = "No obstacle in the conflict area")
	public final static class No_obstacle_in_the_conflict_area extends Obstacle_Detection_Status {}
	public final static No_obstacle_in_the_conflict_area No_obstacle_in_the_conflict_area = new No_obstacle_in_the_conflict_area();
	@JTypeName(s = "Obstacle detected in the conflict area")
	public final static class Obstacle_detected_in_the_conflict_area extends Obstacle_Detection_Status {}
	public final static Obstacle_detected_in_the_conflict_area Obstacle_detected_in_the_conflict_area = new Obstacle_detected_in_the_conflict_area();
}
