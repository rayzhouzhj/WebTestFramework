package com.scmp.framework.utils;

import com.github.lalyos.jfiglet.FigletFont;

import java.io.IOException;

public class Figlet {
	public static void print(String text) {
		String asciiArt = null;
		try {
			asciiArt = FigletFont.convertOneLine(text);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(asciiArt);
	}
}
