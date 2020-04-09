package com.smartism.znzk.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by win7 on 2016/9/12.
 */
public class WeightDataInfo implements Serializable {

    public int total;
    public int index;
    public int size;
    public List<WeightDataBean> result;


    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<WeightDataBean> getResult() {
        return result;
    }

    public void setResult(List<WeightDataBean> result) {
        this.result = result;
    }

    public static class WeightDataBean {
        public long id;//记录ID
        public long time;//时间
        public String  v;//值
        public long mId;//成员ID
        public int t;//值类型
        public  long gid;//类别Id

        public int getT() {
            return t;
        }

        public void setT(int t) {
            this.t = t;
        }

        public long getmId() {
            return mId;
        }

        public void setmId(long mId) {
            this.mId = mId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

        @Override
        public String toString() {
            return "WeightDataBean [ id="+id+", time"+time+", v="+v+"]";
        }

        public long getGid() {
            return gid;
        }

        public void setGid(long gid) {
            this.gid = gid;
        }
    }

    @Override
    public String toString() {
        return  "WeightDataInfo [total="+total+", index="+index+", size="+size+", result="+result+"]";
    }
}
