package com.rayzhou.framework.utils;

import java.io.IOException;

import com.github.lalyos.jfiglet.FigletFont;

public class Figlet 
{
	public static void print(String text) 
	{
		String asciiArt = null;
		try 
		{
			asciiArt = FigletFont.convertOneLine(text);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		System.out.println(asciiArt);
	}
}
