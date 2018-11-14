package entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * created by shonary on 18/10/22
 * email： xiaonaxi.mail@gmail.com
 */
public class GroupConfig {
    @SerializedName("groupid")
    public int groupId;

    @SerializedName("version")
    public int version;

    @SerializedName("title")
    public String title;

    @SerializedName("type")
    public int type;

    @SerializedName("icon")
    public String icon;

    @SerializedName("emotions")
    public List<ConfigItem> emotions;
}
