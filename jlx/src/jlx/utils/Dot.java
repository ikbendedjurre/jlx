package jlx.utils;

public class Dot {
	public static String getRandomColor() {
		switch ((int)(Math.random() * 3)) {
			case 0:
				return "#" + getRandomHex() + getRandomHex() + getRandomHex() + getRandomHex() + "00";
			case 1:
				return "#" + getRandomHex() + getRandomHex() + "00" + getRandomHex() + getRandomHex();
			default:
				return "#" + "00" + getRandomHex() + getRandomHex() + getRandomHex() + getRandomHex();
		}
	}
	
	private static char getRandomHex() {
		return "0123456789ABCDEF".charAt((int)(Math.random() * 16));
	}
}
