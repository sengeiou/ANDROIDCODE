package com.smartism.znzk.domain;

import java.util.ArrayList;
import java.util.List;

public class WeightUserData {  
		  
	    private String mUserName; 
	    private String date1,date2;
	    private List<String> mWeightItem = new ArrayList<String>();  
	  
	    public WeightUserData(String mUserName,String date1) {  
	    	this.mUserName = mUserName; 
	    	this.date1 = date1;
	    }  
	      
	    public String getmUserName() {  
	        return mUserName;  
	    }  
	  
	    public void addItem(String pItemName,String date2) {  
	    	mWeightItem.add(pItemName+"-"+date2);  
	    }  
	      
	    /** 
	     *  获取Item内容 
	     *  
	     * @param pPosition 
	     * @return 
	     */  
	    public String getItem(int pPosition) {  
	        // Category排在第一位  
	        if (pPosition == 0) {  
	            return mUserName+":"+date1;  
	        } else {  
	            return mWeightItem.get(pPosition - 1);  
	        }  
	    }  
	      
	    /** 
	     * 当前类别Item总数。Category也需要占用一个Item 
	     * @return  
	     */  
	    public int getItemCount() {  
	        return mWeightItem.size() + 1;  
	    }  
	      
	}  


