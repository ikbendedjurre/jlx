package jlx.asal.parsing.api;

import java.util.List;

import jlx.asal.parsing.*;
import jlx.common.FileLocation;
import jlx.utils.*;

public abstract class ASALStatement extends ASALSyntaxTreeAPI {
	private FileLocation fileLocation;
	
	public ASALStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
	}
	
	public void confirmReturnValue() throws ASALException {
		throw new ASALException(this, "Operation may not return a value!");
	}
	
	public abstract boolean containsNonEmpty();
	
	public FileLocation getFileLocation() {
		return fileLocation;
	}
	
	public void setFileLocation(FileLocation fileLocation) {
		this.fileLocation = fileLocation;
	}
	
	public String toText(TextOptions options) {
		return Texts.concat(toText(new Indentation(""), options), " ");
	}
	
	public abstract List<String> toText(Indentation indent, TextOptions options);
}
