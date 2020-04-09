package com.smartism.znzk.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2017/4/5.
 */

public class ImageBannerInfo implements Serializable {

    public List<ImageBannerBean> bannerBeanList = new ArrayList<>();

    public List<ImageBannerBean> getBannerBeanList() {
        return bannerBeanList;
    }

    public void setBannerBeanList(List<ImageBannerBean> bannerBeanList) {
        this.bannerBeanList = bannerBeanList;
    }

    public static class ImageBannerBean implements Serializable{
        private String name;
        private String content;
        private String urlType;
        private String url;
        private String lang;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUrlType() {
            return urlType;
        }

        public void setUrlType(String urlType) {
            this.urlType = urlType;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        @Override
        public String toString() {
            return "BannerBean [ name =" + name + ", url = " + url + " ]";
        }
    }
}
