package jlx.behave;

/**
 * Final states are stable, so they must be states!
 */
public abstract class FinalState extends State {
	@Override
	public final LocalTransition onEntry() {
		return null;
	}
	
	@Override
	public final LocalTransition[] onDo() {
		return null;
	}
	
	@Override
	public final LocalTransition onExit() {
		return null;
	}
	
	@Override
	public final Outgoing[] getOutgoing() {
		return null;
	}
}
