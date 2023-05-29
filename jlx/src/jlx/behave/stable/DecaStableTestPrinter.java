package jlx.behave.stable;

import java.io.*;
import java.util.*;

public class DecaStableTestPrinter {
	public static void print(List<DecaStableTest> tests, String dirName) {
		for (DecaStableTest test : tests) {
			test.print(dirName);
		}
		
		try {
			PrintStream out = new PrintStream(dirName + "/ATestSuite.vb");
			out.println("Imports Testing");
			out.println("Public Class ATestSuite");
			out.println("\tInherits TestSuite");
			out.println("\tPublic Overrides Function GetTests() As Test()");
			out.println("\t\tReturn New Test() {");
			
			if (tests.size() > 0) {
				Iterator<DecaStableTest> q = tests.iterator();
				
				for (int index = 1; index < tests.size(); index++) {
					out.println("\t\t\tNew " + q.next().getName() + "(),");
				}
				
				out.println("\t\t\tNew " + q.next().getName() + "()"); //(No comma.)
			}
			
			out.println("\t\t}");
			out.println("End Function");
			out.println("End Class");
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new Error(e);
		}
	}
}

