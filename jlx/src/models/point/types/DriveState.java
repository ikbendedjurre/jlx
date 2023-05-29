package models.point.types;

import jlx.asal.j.*;

public class DriveState extends JUserType<DriveState> {
	@JTypeName(s = "STOPPED")
	public final static class STOPPED extends DriveState {}
	@JTypeName(s = "MOVING")
	public final static class MOVING extends DriveState {}
	@JTypeName(s = "")
	public final static class NONE extends DriveState {}
}
