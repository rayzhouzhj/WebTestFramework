package com.github.framework.utils;

import java.util.ArrayList;

public class PixelMatch {

    public class PixelMatchResult {
        public boolean result;
        public ArrayList<String> output;

        public PixelMatchResult(boolean result, ArrayList<String> output) {
            this.result = result;
            this.output = output;
        }
    }

    public PixelMatchResult match(String image1, String image2, String output) {
        ArrayList<String> allLine = null;
        try {
            allLine = new CommandPrompt().runCommand("pixelmatch");
            for (String s : allLine) {
                System.out.println(s);
            }
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }

        return new PixelMatch.PixelMatchResult(false, allLine);
    }
}
