package com.smartism.znzk.xiongmai.lib.funsdk.support.config;

public interface JsonListener {
	String getSendMsg();

	boolean onParse(String json);
}
