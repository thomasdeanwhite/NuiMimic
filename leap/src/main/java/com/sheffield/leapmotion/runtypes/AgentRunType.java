package com.sheffield.leapmotion.runtypes;

import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;

import java.io.IOException;

/**
 * Created by thomas on 18/11/2016.
 */
public class AgentRunType implements RunType {

    @Override
    public int run() {
        String command = "java -javaagent:lm-agent.jar -jar " + Properties.JAR_UNDER_TEST;
        App.out.print("Executing command: \n\t" + command);
        try {
            Process p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }
}
