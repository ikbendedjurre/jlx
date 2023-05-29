package jlx.printing;

import java.io.*;

public class CLI {
	private final static boolean WINDOWS = true;
	
	public static void mcrl22lps(String inFile, String outFile) {
		if (WINDOWS) {
			cmd("wsl", "mcrl22lps", "-v", inFile + ".mcrl2", outFile + "0.lps");
			cmd("wsl", "lpssuminst", "-v", "-f", outFile + "0.lps", outFile + "1.lps");
			cmd("wsl", "lpssumelm", "-v", outFile + "1.lps", outFile + "2.lps");
			cmd("wsl", "lpsrewr", "-v", outFile + "2.lps", outFile + "3.lps");
		} else {
			cmd("mcrl22lps", "-v", inFile + ".mcrl2", outFile + "0.lps");
			cmd("lpssuminst", "-v", "-f", outFile + "0.lps", outFile + "1.lps");
			cmd("lpssumelm", "-v", outFile + "1.lps", outFile + "2.lps");
			cmd("lpsrewr", "-v", outFile + "2.lps", outFile + "3.lps");
		}
	}
	
	public static void mcrl22lps(String inFile, String outFile, boolean useSumInst) {
		if (useSumInst) {
			mcrl22lps(inFile, outFile);
		} else {
			if (WINDOWS) {
				cmd("wsl", "mcrl22lps", "-v", inFile + ".mcrl2", outFile + "0.lps");
				cmd("wsl", "lpssumelm", "-v", outFile + "0.lps", outFile + "2.lps");
				cmd("wsl", "lpsrewr", "-v", outFile + "2.lps", outFile + "3.lps");
			} else {
				cmd("mcrl22lps", "-v", inFile + ".mcrl2", outFile + "0.lps");
				cmd("lpssumelm", "-v", outFile + "0.lps", outFile + "2.lps");
				cmd("lpsrewr", "-v", outFile + "2.lps", outFile + "3.lps");
			}
		}
	}
	
	private static void cmd(String... cmdArray) {
		try {
			ProcessBuilder pb = new ProcessBuilder(cmdArray);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			
			while ((line = input.readLine()) != null) {
				System.out.println("> " + line);
				
				if (line.contains("[error]"))
				{
					throw new Error("Problem in the mCRL2 tool chain!");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
