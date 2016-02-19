package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public class Branch {
	private String className;
	private int lineNumber;
	private int trueHits;
	private int falseHits;

	public Branch(String className, int lineNumber) {
		this.className = className;
		this.lineNumber = lineNumber;
	}

	public void trueHit(int trueHits) {
		this.trueHits += trueHits;
	}

	public void falseHit(int falseHits) {
		this.falseHits += falseHits;
	}

	public int getTrueHits() {
		return trueHits;
	}

	public int getFalseHits() {
		return falseHits;
	}

	public void reset() {
		trueHits = 0;
		falseHits = 0;
	}

	public String getClassName() {
		return className;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String toString() {
		return lineNumber + "#true, " + lineNumber + "#false";
	}

	public boolean equals(Branch other) {
		if (this == other) {
			return true;
		}
		if (lineNumber == other.lineNumber && className.equals(other.className)) {
			return true;
		}
		return false;
	}

}
