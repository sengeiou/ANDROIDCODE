package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * Created by win7 on 2017/11/8.
 */

public class PersInfo implements Serializable{
    private long zj_id;
    private long id;
    private String k;
    private String v;

    public long getZj_id() {
        return zj_id;
    }

    public void setZj_id(long zj_id) {
        this.zj_id = zj_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }


    public enum UserPerssionKey {

        p_add("p_cd_add"),
        p_del("p_cd_del");

        private String value;

        UserPerssionKey(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
