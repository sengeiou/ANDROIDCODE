package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * Created by win7 on 2017/5/13.
 */

public class SmartMedicineBean implements Serializable {

    private long id;
    private String logo;
    private String lname;
    private String unit;
    private int dosage;
    private String takeMedicineCycle;
    private String afterOrBeforeEat;
    private int total;
    private String addTime;
    private boolean stock;

    public boolean isStock() {
        return stock;
    }

    public void setStock(boolean stock) {
        this.stock = stock;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getDosage() {
        return dosage;
    }

    public void setDosage(int dosage) {
        this.dosage = dosage;
    }

    public String getTakeMedicineCycle() {
        return takeMedicineCycle;
    }

    public void setTakeMedicineCycle(String takeMedicineCycle) {
        this.takeMedicineCycle = takeMedicineCycle;
    }

    public String getAfterOrBeforeEat() {
        return afterOrBeforeEat;
    }

    public void setAfterOrBeforeEat(String afterOrBeforeEat) {
        this.afterOrBeforeEat = afterOrBeforeEat;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }


}
