package org.skynetsoftware.broadcast_actions_list_manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;

public class Main {
    private static final String BROADCAST_ACTIONS_FILE = "/Users/apple/workspace/event-logger/app/src/main/res/raw/broadcast_actions.txt";

    public static void main(String[] args) throws IOException {
        Process p = Runtime.getRuntime().exec("adb shell dumpsys activity b");
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

        Set<String> actionsDumpsys = new HashSet<String>();

        String line;
        while ((line = r.readLine()) != null) {

            if(line.startsWith("      Action:")) {
                line = line.substring(15, line.length() - 1);
                actionsDumpsys.add(line);
            }
        }

        Set<String> actionsCurrent = new HashSet<String>();
        r = new BufferedReader(new InputStreamReader(new FileInputStream(BROADCAST_ACTIONS_FILE)));

        while ((line = r.readLine()) != null) {

            actionsCurrent.add(line);
        }

        actionsDumpsys.removeAll(actionsCurrent);

        if(!actionsDumpsys.isEmpty()) {

            StringJoiner joiner = new StringJoiner("\n");
            for (String action : actionsDumpsys) {
                joiner.add(action);
            }

            System.out.println("New Actions");
            System.out.println(joiner.toString());

            Scanner sc = new Scanner(System.in);
            System.out.println("Do you want to add them to broadcast_actions.txt (y/N):");
            String res = sc.next();

            if("y".equalsIgnoreCase(res)) {

                BufferedWriter bw = new BufferedWriter(new FileWriter(BROADCAST_ACTIONS_FILE, true));
                for(String action : actionsDumpsys)
                {
                    bw.write(action);
                    bw.newLine();
                }
                bw.flush();

                System.out.println("Added");
            }
            else {
                System.out.println("Not added");
            }
        }
        else
        {
            System.out.println("No new actions");
        }

    }
}
