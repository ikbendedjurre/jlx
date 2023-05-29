package jlx.behave;

import jlx.asal.j.*;
import jlx.asal.parsing.ASALCode;

public class LocalTransition extends CodeObject {
	public LocalTransition(String code0, String... code) {
		super(new ASALCode("TRANSITION", code0, code));
	}
	
	public LocalTransition(JEvt evt) {
		super(new ASALCode("TRANSITION", evt));
	}
}
