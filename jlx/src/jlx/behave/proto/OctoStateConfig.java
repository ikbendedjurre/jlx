package jlx.behave.proto;

import java.util.*;

public class OctoStateConfig {
	public final Set<SeptaVertex> states;
	
	public OctoStateConfig(Set<SeptaVertex> states) {
		this.states = states;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(states);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof OctoStateConfig)) {
			return false;
		}
		OctoStateConfig other = (OctoStateConfig) obj;
		return Objects.equals(states, other.states);
	}
}
