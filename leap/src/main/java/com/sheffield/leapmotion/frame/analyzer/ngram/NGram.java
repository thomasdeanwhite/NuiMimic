package com.sheffield.leapmotion.frame.analyzer.ngram;

import java.util.ArrayList;

/**
 * Created by Thomas on 06-02-2017.
 */
public class NGram {

    protected String element;
    

    protected int count = 0;
    protected float probability = 0f;

    protected ArrayList<NGram> children;

    protected int n = 0;

    protected boolean finalised = false;
    public NGram (String label){
        children = new ArrayList<NGram>();
        element = label;
    }


    public void add(String text){
        block();
        NGram child = null;

        if (!text.contains(NGramModel.DELIMITER)){
            child = restore(text);
        } else {
            String[] newText = text.split(NGramModel.DELIMITER);
            child = restore(newText[0]);

            if (n < newText.length){
                n = newText.length;
            }

            String childText = text.substring(text.indexOf(NGramModel.DELIMITER)+1);

            child.add(childText);
        }

        child.increment();

        if (!children.contains(child)) {
            children.add(child);
        }
    }

    private NGram restore(String s){
        for (NGram n : children){
            if (n.element.equals(s)){
                return n;
            }
        }
        return new NGram(s);
    }


    public void increment(){
        block();
        count++;
    }

    private void block(){
        if (finalised){
            throw new IllegalStateException("Cannot modify NGram once finalised.");
        }
    }

    public void calculateProbabilities(){

        int total = 0;

        finalised = true;

        for (NGram n: children){
            n.calculateProbabilities();
            total += n.count;
        }

        for (NGram n: children){
            n.probability = n.count / (float) total;
        }
    }

    private String toLine(String current){
        current += NGramModel.DELIMITER + element;
        if (children.size() == 0){
            return current;
        }
        String s = "";
        for (NGram n : children){
            s += n.toLine(current).trim() + "\n";
        }
        return s;
    }

    public float getProbability(String child){

        String[] cs = child.split(NGramModel.DELIMITER);

        for (NGram n : children){
            if (n.element.equals(cs[0])){
                return n.getProbability(child.substring(child.indexOf(NGramModel.DELIMITER)+1));
            }
        }

        return probability;
    }

    public String toString(){

        String ngram = "";

        for (NGram n : children){
            ngram += n.toLine(element);
        }

        return ngram;
    }

}
