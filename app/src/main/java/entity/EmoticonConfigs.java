package entity;

import java.util.List;

import network.BaseModel;


/**
 * created by shonary on 18/10/22
 * email： xiaonaxi.mail@gmail.com
 */
public class EmoticonConfigs extends BaseModel {
    private List<TabConfig> data;

    public List<TabConfig> getData() {
        return data;
    }

    public void setData(List<TabConfig> data) {
        this.data = data;
    }

    public static class TabConfig {
        public int tabId;

        public int groupId;

        public int groupVersion;

        public String downloadUrl;

        public String md5;

        @Override
        public boolean equals(Object o) {
            if(o instanceof TabConfig){
                TabConfig config = (TabConfig)o;
                if(groupId == config.groupId){
                    if(groupVersion == config.groupVersion){
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "TabConfig{" +
                    "tabId=" + tabId +
                    ", groupId=" + groupId +
                    ", groupVersion=" + groupVersion +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    ", md5='" + md5 + '\'' +
                    '}';
        }
    }
}
