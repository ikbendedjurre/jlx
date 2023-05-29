package jlx.blocks.ibd1;

import java.util.*;

public class VerificationAction {
	private final VerificationModel model;
	private final String id;
	
	public VerificationAction(VerificationModel model, String id) {
		this.model = model;
		this.id = id;
	}
	
	public VerificationModel getModel() {
		return model;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, model);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VerificationAction other = (VerificationAction) obj;
		return Objects.equals(id, other.id) && Objects.equals(model, other.model);
	}
}

