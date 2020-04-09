//package com.smartism.znzk.domain;
//
//import java.io.Serializable;
//
///**
// * Created by win7 on 2017/10/24.
// */
//
//public class UserPermissonInfo implements Serializable {
//
//
//    //    p_cd_add(permission_childdevice_add)
////    p_cd_add(permission_childdevice_del)
//    private String k;//权限标识
////    private String n;//权限名称
//    private int v;//1有权限 0无权
//    private long id;
//    private long zj_id;
//
//    public long getZj_id() {
//        return zj_id;
//    }
//
//    public void setZj_id(long zj_id) {
//        this.zj_id = zj_id;
//    }
//
//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
//
//    public String getK() {
//        return k;
//    }
//
//    public void setK(String k) {
//        this.k = k;
//    }
//
////    public String getN() {
////        return n;
//////    }
////
////    public void setN(String n) {
////        this.n = n;
////    }
//
//    public int getV() {
//        return v;
//    }
//
//    public void setV(int v) {
//        this.v = v;
//    }
//
//    @Override
//    public String toString() {
//        return "UserPermissonInfo{" + "k=" + k
//                + ", zj_id=" + zj_id
//                + ", v=" + v + "}";
//    }
//
//    public enum UserPerssionKey {
//
//        p_add("p_cd_add"),
//        p_del("p_cd_del");
//
//        private String value;
//
//        UserPerssionKey(String value) {
//            this.value = value;
//        }
//
//        public String value() {
//            return value;
//        }
//    }
//}
