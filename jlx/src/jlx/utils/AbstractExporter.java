package jlx.utils;

import java.io.*;

public abstract class AbstractExporter {
	public final void saveToFile(String filename) {
		try {
			File targetFile = new File(filename);
			targetFile.getCanonicalFile().getParentFile().mkdirs();
			PrintStream out = new PrintStream(new File(filename));
			saveToFile(out);
			out.close();
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	public abstract void saveToFile(PrintStream out);
}
