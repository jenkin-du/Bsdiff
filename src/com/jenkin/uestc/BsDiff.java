package com.jenkin.uestc;

public class BsDiff {

	static {
		System.loadLibrary("Bsdiff");
	}
	
	public static native int genDiff(String oldApkPath, String newApkPath, String pathPath);
}
