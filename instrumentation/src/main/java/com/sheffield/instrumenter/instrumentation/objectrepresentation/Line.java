package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public class Line {
	private String className;
	private int lineNumber;
	private int hits;

	public Line(String className, int lineNumber) {
		this.className = className;
		this.lineNumber = lineNumber;
	}

	public void hit(int newHits) {
		this.hits += newHits;
	}

	public int getHits() {
		return hits;
	}

	public void reset() {
		hits = 0;
	}

	public String getClassName() {
		return className;
	}

	public int getLineNumber() {
		return lineNumber;
	}
}
