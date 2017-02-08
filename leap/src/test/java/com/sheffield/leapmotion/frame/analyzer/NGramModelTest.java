package com.sheffield.leapmotion.frame.analyzer;

import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Thomas on 06-02-2017.
 */
public class NGramModelTest {

    private NGram ng;



    @Test
    public void createNGram(){
        String sentence = "That that is is that that is not is not Is that it It is".toLowerCase();

        ng = NGramModel.getNGram(2, sentence);

        assertEquals("that that is\n" +
                "that is is\n" +
                "that is not\n" +
                "that it it\n" +
                "is is that\n" +
                "is that that\n" +
                "is that it\n" +
                "is not is\n" +
                "not is not\n" +
                "not is that\n" +
                "it it is\n", ng.toString());
    }


    @Test
    public void checkProbabilities(){
        String sentence = "That that is is that that is not is not Is that it It is".toLowerCase();

        ng = NGramModel.getNGram(1, sentence);

        ng.calculateProbabilities();

        assertEquals(0.4f, ng.getProbability("that that"), 0.00000001f);
    }

}
