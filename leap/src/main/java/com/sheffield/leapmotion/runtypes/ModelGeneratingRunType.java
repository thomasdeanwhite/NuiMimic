package com.sheffield.leapmotion.runtypes;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.sampler.SamplerApp;
import com.sheffield.leapmotion.util.ProgressBar;

/**
 * Created by thomas on 18/11/2016.
 */
public class ModelGeneratingRunType implements RunType {

    @Override
    public int run() {

        App.out.println("- Starting Model Generation");
        SeededController sc = SeededController.getSeededController(false);

        if (Properties.VISUALISE_DATA) {
            App.DISPLAY_WINDOW = new DisplayWindow();
        }

        App.setOutput();

        App.out.println(ProgressBar.getHeaderBar(21));

        float progress = 0f;

        while (SeededController.getSeededController().hasNextFrame()) {
            long time = System.currentTimeMillis();

            if (sc.lastTick() < time) {
                sc.tick(time);
            }

            float prog = SeededController.getSeededController().getProgress();

            if (Properties.SHOW_PROGRESS  || prog > progress) {
                progress += 0.1f;

                App.out.print("\r" + ProgressBar.getProgressBar(21, prog) +
                        SeededController.getSeededController().status());
            }
        }

        App.out.println("- Finished Model Generation");

        return 0;
    }
}
