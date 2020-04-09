package com.smartism.znzk.domain;

import com.smartism.znzk.util.StringUtils;

import java.util.Locale;

public class TracerouteContainer {

    private int ttl;
    private String hostname;
    private String ip;
    private float elapsedtime;

    public TracerouteContainer(int i,String s, String s1, float elapsedTime) {
        ttl = i;
        hostname=s;
        ip=s1;
        this.elapsedtime=elapsedTime;
    }


    public String getIp() {
        return ip;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString(){
        if (!StringUtils.isEmpty(ip)){
            return ttl+". "+hostname+"             "+String.format(Locale.ENGLISH,"%.1f",elapsedtime)+"ms";
        }
        return "**********";
    }
}