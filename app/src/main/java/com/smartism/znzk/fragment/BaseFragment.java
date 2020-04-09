package com.smartism.znzk.fragment;

import android.support.v4.app.Fragment;

import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.ImputDialog;
import com.smartism.znzk.widget.NormalDialog;

public class BaseFragment extends Fragment {
	private boolean isRun = false;
	public NormalDialog dialog;
	public ImputDialog inputDialog;

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isRun = false;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isRun = true;
	}

	public boolean getIsRun() {
		return isRun;
	}
	/**
	 * 弹出等待框
	 * @param listener 消失监听
	 * @param mark 等待框标记
	 */
	public void showLoadingDialog(NormalDialog.OnCustomCancelListner listener, int mark){
		dialog=new NormalDialog(getActivity());
		dialog.setOnCustomCancelListner(listener);
		dialog.setLoadingMark(mark);
		dialog.showLoadingDialog();
	}

	/**
	 * 展示输入框
	 * @param inputClickListner
	 * @param title
	 * @param hintstring
	 * @param etx
	 * @param Yes
	 * @param No
	 */
	public void showInputDialog(ImputDialog.MyInputClickListner inputClickListner, int title, int hintstring, String etx, int Yes, int No){
		inputDialog=new ImputDialog(getActivity());
		inputDialog.setEditextHint(Utils.getStringForId(hintstring));
		inputDialog.setInputTitle(Utils.getStringForId(title));
		inputDialog.setEdtextText(etx);
		inputDialog.setYes(Utils.getStringForId(Yes));
		inputDialog.setNo(Utils.getStringForId(No));
		inputDialog.setOnMyinputClickListner(inputClickListner);
		inputDialog.show();
	}

}
