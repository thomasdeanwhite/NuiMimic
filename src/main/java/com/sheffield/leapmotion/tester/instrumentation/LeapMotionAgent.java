package com.sheffield.leapmotion.tester.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

import com.sheffield.leapmotion.tester.App;

public class LeapMotionAgent {

	public static void premain(String args, Instrumentation inst) {
        System.out.println(" _        _______     _________ _______  _______ _________");
        System.out.println("( \\      (       )    \\__   __/(  ____ \\(  ____ \\\\__   __/");
        System.out.println("| (      | () () |       ) (   | (    \\/| (    \\/   ) (");
        System.out.println("| |      | || || | _____ | |   | (__    | (_____    | |");
        System.out.println("| |      | |(_)| |(_____)| |   |  __)   (_____  )   | |");
        System.out.println("| |      | |   | |       | |   | (            ) |   | |");
        System.out.println("| (____/\\| )   ( |       | |   | (____/\\/\\____) |   | |");
        System.out.println("(_______/|/     \\|       )_(   (_______/\\_______)   )_(");
        System.out.println("- Successfuly loaded agent premain with options " + args + ".");
        inst.addTransformer(new ClassReplacementTransformer());
        String[] opts = null;
        if (args != null) {
            args = args.replace(":", " ");


            ArrayList<String> newList = new ArrayList<String>();
            for (String s : args.split(",")) {
                for (String s1 : s.split(" ")) {
                    newList.add(s1);
                }
            }
            opts = new String[newList.size()];
            newList.toArray(opts);

        }
        App.background(opts);
	}

	public static void agentmain(String args, Instrumentation inst) {
		premain(args, inst);
	}

}
