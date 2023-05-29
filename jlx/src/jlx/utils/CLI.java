package jlx.utils;

import java.io.IOException;

public class CLI {
	public static void waitForEnter() {
		try {
			System.out.println(". . . press enter to continue . . .");
			
			while (System.in.read() != Character.LINE_SEPARATOR) {
				//Do nothing.
			}
		} catch (IOException e) {
			throw new Error(e);
		}
	}
}
