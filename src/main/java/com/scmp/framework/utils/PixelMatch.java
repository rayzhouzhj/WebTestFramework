package com.scmp.framework.utils;

import java.io.File;
import java.util.ArrayList;

public class PixelMatch {

    private double threshold = 0.005d;

    public PixelMatch() {
    }

    public PixelMatch(double threshold) {
        this.threshold = threshold;
    }

    public class PixelMatchResult {
        public boolean IsMatched;
        public String MatchIn;
        public String DifferentPixels;
        public String Error;
        public ArrayList<String> Details;

        public PixelMatchResult(String matchIn, String differentPixels, String error, ArrayList<String> details) {
            this.MatchIn = matchIn;
            this.DifferentPixels = differentPixels;
            this.Error = error;
            this.Details = details;

            if (this.Error.equalsIgnoreCase("0%")) {
                this.IsMatched = true;
            }
        }

        @Override
        public String toString(){
            String output = "";
            for (int i = 0; i < this.Details.size(); i++) {
                output = output + this.Details.get(i) + "\n";
            }

            return output;
        }
    }

    public PixelMatchResult match(String image1, String image2, String outputFile) {
        ArrayList<String> allLine = null;
        String matchIn = "";
        String differentPixels = "";
        String error = "";
        String image1Path = new File(image1).toPath().toAbsolutePath().toString();
        String image2Path = new File(image2).toPath().toAbsolutePath().toString();

        try {
            allLine = new CommandPrompt().runCommand("pixelmatch " + image1Path + " " + image2Path + " " + outputFile + " " + this.threshold);
            for (String line : allLine) {
                System.out.println(line);
                String[] data = line.split(":");
                if (data[0].trim().equalsIgnoreCase("matched in")) {
                    matchIn = data[1].trim();
                } else if (data[0].trim().equalsIgnoreCase("different pixels")) {
                    differentPixels = data[1].trim();
                } else if (data[0].trim().equalsIgnoreCase("error")) {
                    error = data[1].trim();
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }

        return new PixelMatch.PixelMatchResult(matchIn, differentPixels, error, allLine);
    }
}
