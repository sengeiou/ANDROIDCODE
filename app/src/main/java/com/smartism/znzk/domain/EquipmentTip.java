package com.smartism.znzk.domain;

public class EquipmentTip {

	private long id;
	private String tip;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTip() {
		return tip;
	}
	public void setTip(String tip) {
		this.tip = tip;
	}
	
	@Override
	public int hashCode() {
		return String.valueOf(id).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return this.id == ((EquipmentTip)o).id;
	}
	
	@Override
	public String toString() {
		return "EquipmentTip [id=" + id + ", tip=" + tip + "]";
	}
	

}
