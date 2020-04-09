package com.smartism.znzk.xiongmai.lib.sdk.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**预置点功能<br/>
 * 为了简洁将巡航线功能额外新建了一个:{@link com.xworld.devset.tour.model.bean.OPPTZControlBean}
 */

public class OPPPanoramaPTZControlBean {
    public static final int OPPTZCONTROL_ID = 1400;
    public static final String JSON_NAME = "OPPTZControl";
    public static final String PANORAMA = "GoToPosition";
    @JSONField(name = "Command")
    private String command;//预置点操作类别
    @JSONField(name = "Parameter")
    private Parameter parameter = new Parameter();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public class Parameter {
        @JSONField(name = "POINT")
        private POINT point = new POINT();

        public POINT getPoint() {
            return point;
        }

        public void setPoint(POINT point) {
            this.point = point;
        }

        public class POINT {
           @JSONField(name = "left")
           private int left;//电机横向偏移步数
           @JSONField(name = "top")
           private int top;//电机纵向偏移步数

            public int getLeft() {
                return left;
            }

            public void setLeft(int left) {
                this.left = left;
            }

            public int getTop() {
                return top;
            }

            public void setTop(int top) {
                this.top = top;
            }
        }
    }
}
