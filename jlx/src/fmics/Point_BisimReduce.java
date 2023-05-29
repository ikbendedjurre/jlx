package fmics;

import java.io.IOException;

import jlx.behave.stable.files.*;
import jlx.common.reflection.ReflectionException;

public class Point_BisimReduce {
	public static void main(String[] args) throws ReflectionException, IOException {
		DecaStableFileBisimReducer r = new DecaStableFileBisimReducer();
		r.loadFromFile("output", "point.sic", false);
		
		DecaStableFileWriter dest = new DecaStableFileWriter();
		r.writeTo(dest);
		dest.saveToFile("output", "point.bisim.sic");
	}
}

