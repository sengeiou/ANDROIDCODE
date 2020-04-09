package com.smartism.znzk.util;

public interface Callback {
	void onBefore();

	boolean onRun();

	void onAfter(boolean b);
}
