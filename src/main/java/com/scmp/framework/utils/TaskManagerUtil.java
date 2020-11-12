package com.scmp.framework.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerUtil {
    public static List<String> findProcess(String processName) {
        List<String> processList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            // For windows
            if(System.getProperty("os.name").contains("Windows"))
            {
                String command = "tasklist /FI \"IMAGENAME eq " + processName + "\"";
                Process proc = CommandPrompt.executeCommand(command);

                br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains(processName)) {
                        line = line.replaceAll("\\s+", " ");
                        String[] list = line.split(" ");
                        processList.add(list[1]);
                    }
                }
            }
            // For Mac
            else
            {
                String command = "ps aux | grep \"" + processName + "\" | grep -v 'grep' | awk '{print $2}'";
                Process proc = CommandPrompt.executeCommand(command);

                br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    processList.add(line);
                }
            }

            return processList;
        } catch (Exception e) {
            e.printStackTrace();
            return processList;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ex) {
                }
            }

        }
    }

    public static void killProcess(String processID) {
        BufferedReader br = null;
        try {
            String command;
            // For windows
            if(System.getProperty("os.name").contains("Windows"))
            {
                command = "Taskkill /F /PID " + processID;
            }
            // For MAC
            else
            {
                command = "kill " + processID;
            }

            Process proc = CommandPrompt.executeCommand(command);

            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);

                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String string_Temp = in.readLine();
                while (string_Temp != null)
                {
                    System.out.println(string_Temp);
                    string_Temp = in.readLine();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ex) {
                }
            }

        }
    }
}
