package com.smartism.znzk.util.indexlistsort;

/**
 * 该对象通过 sortLetters属性进行排序后，显示在listview中
 * @author wtw
 * 2015-10-16上午9:08:57
 */
public class SortModel {

	private String name;   //显示的数据
	private String sortLetters;  //显示数据拼音的首字母（一定需要的属性，用来进行排序）
	private String _short;//简写
	private String pinyin  ; //全拼
	private String firstTwoLetters ; //每个汉字首字母，英文是全拼

	public String getFirstTwoLetters(){
		return firstTwoLetters;
	}

	public void setFirstTwoLetters(String letters){
		this.firstTwoLetters = letters;
	}




	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int id ;

	public int getBrandName() {
		return brandName;
	}

	public void setBrandName(int brandName) {
		this.brandName = brandName;
	}

	private int brandName;//品牌名称
	public String get_short() {
		return _short;
	}
	public void set_short(String _short) {
		this._short = _short;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
