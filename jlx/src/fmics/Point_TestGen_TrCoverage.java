package fmics;

import java.io.IOException;

import jlx.behave.stable.files.*;
import jlx.behave.stable.testgen.DecaStableTestGen5;
import jlx.common.reflection.ReflectionException;

public class Point_TestGen_TrCoverage {
	public static void main(String[] args) throws ReflectionException, IOException {
		DecaStableFileWriter r = new DecaStableFileWriter();
		r.loadFromFile("output", "point.bisim.sic", false);
		
		new DecaStableTestGen5(r, 2f, false).populateTests();
		r.saveToFile("output", "point.trcov.sic");
	}
}

