package com.sheffield.leapmotion.runtypes;

import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.instrumentation.MockSystem;
import com.scythe.output.Csv;

/**
 * Created by thomas on 18/11/2016.
 */
public class ReconstructingRunType implements RunType {

    @Override
    public int run() {
        while (Properties.INPUT.length > 0) {

            SeededController sc = SeededController.getSeededController(false);

            Properties.FRAME_SELECTION_STRATEGY = Properties
                    .FrameSelectionStrategy.RECONSTRUCTION;

            Properties.CURRENT_RUN = 0;

            long startTime = System.currentTimeMillis();
            long time = startTime;
            long endTime = time + Properties.RUNTIME;
            while ((time = System.currentTimeMillis()) < endTime) {
                if (sc.lastTick() < time) {
                    sc.tick(time);
                }
                sc.frame();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Csv csv = new Csv();

            csv.add("rootMeanSquared", "" + sc.status().split("rms: ")[1]);

            //Properties.OUTPUT_INCLUDES_ARRAY.add("gestureFiles");

            csv.add("input", Properties.INPUT[0]);

//            csv.merge(Properties.instance().toCsv());

            csv.add("frameSelectionStrategy", Properties
                    .FRAME_SELECTION_STRATEGY.toString());

            MockSystem.MILLIS = (int) (time - startTime);

            csv.add("runtime", "" + MockSystem.MILLIS);

            csv.finalize();
            App.getApp().output(csv);

            String[] gFiles = Properties.INPUT;

            if (gFiles.length > 1) {
                Properties.INPUT = new String[gFiles.length - 1];

                for (int i = 1; i < gFiles.length; i++) {
                    Properties.INPUT[i - 1] = gFiles[i];
                }
            } else {
                break;
            }

            SeededController.getSeededController().cleanUp();
            SeededController.resetSeededController();
        }

        //System.exit(0);
        return 0;
    }
}
