package models.generic.types;

import jlx.asal.j.*;

public class PDI_Connection_State extends JUserType<PDI_Connection_State> {
	@JTypeName(s = "")
	public final static class NONE extends PDI_Connection_State {}
	@JTypeName(s = "CLOSED")
	public final static class CLOSED extends PDI_Connection_State {}
	public final static CLOSED CLOSED = new CLOSED();
	@JTypeName(s = "CLOSED_REQUESTED")
	public final static class CLOSED_REQUESTED extends PDI_Connection_State {}
	@JTypeName(s = "WAITING_FOR_VERSION_CHECK")
	public final static class WAITING_FOR_VERSION_CHECK extends PDI_Connection_State {}
	@JTypeName(s = "WAITING_FOR_INITIALISATION")
	public final static class WAITING_FOR_INITIALISATION extends PDI_Connection_State {}
	@JTypeName(s = "RECEIVING_STATUS")
	public final static class RECEIVING_STATUS extends PDI_Connection_State {}
	public final static RECEIVING_STATUS RECEIVING_STATUS = new RECEIVING_STATUS();
	@JTypeName(s = "ESTABLISHED")
	public final static class ESTABLISHED extends PDI_Connection_State {}
	public final static ESTABLISHED ESTABLISHED = new ESTABLISHED();
	@JTypeName(s = "INIT_TIMEOUT")
	public final static class INIT_TIMEOUT extends PDI_Connection_State {}
	@JTypeName(s = "VERSION_UNEQUAL")
	public final static class VERSION_UNEQUAL extends PDI_Connection_State {}
	@JTypeName(s = "CHECKSUM_UNEQUAL")
	public final static class CHECKSUM_UNEQUAL extends PDI_Connection_State {}
	@JTypeName(s = "PROTOCOL_ERROR")
	public final static class PROTOCOL_ERROR extends PDI_Connection_State {}
	public final static PROTOCOL_ERROR PROTOCOL_ERROR = new PROTOCOL_ERROR();
	@JTypeName(s = "TELEGRAM_ERROR")
	public final static class TELEGRAM_ERROR extends PDI_Connection_State {}
	public final static TELEGRAM_ERROR TELEGRAM_ERROR = new TELEGRAM_ERROR();
	@JTypeName(s = "IMPERMISSIBLE")
	public final static class IMPERMISSIBLE extends PDI_Connection_State {}
	
	@JTypeName(s = "CLOSED_READY")
	public final static class CLOSED_READY extends PDI_Connection_State {}
	@JTypeName(s = "CLOSING")
	public final static class CLOSING extends PDI_Connection_State {}
	public final static CLOSING CLOSING = new CLOSING();
	@JTypeName(s = "READY_FOR_VERSION_CHECK")
	public final static class READY_FOR_VERSION_CHECK extends PDI_Connection_State {}
	@JTypeName(s = "SENDING_STATUS")
	public final static class SENDING_STATUS extends PDI_Connection_State {}
	public final static SENDING_STATUS SENDING_STATUS = new SENDING_STATUS();
	@JTypeName(s = "READY_FOR_INITIALISATION")
	public final static class READY_FOR_INITIALISATION extends PDI_Connection_State {}
}
