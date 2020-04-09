package com.smartism.znzk.util.update;

public interface UpdateListener {
	public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo);
}
