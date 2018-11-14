package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * created by shonary on 18/10/23
 * email： xiaonaxi.mail@gmail.com
 */
public class EmoticonEntity {

    public String title;

    public String icon;

    //0：unicode ；1：static image；2: gif
    public int type;

    public int groupId;

    public int version;

    public List<EmoticonEntityItem> items;

    public EmoticonEntity() {
        items = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof EmoticonEntity){
            EmoticonEntity emoji = (EmoticonEntity)o;
            if(groupId == emoji.groupId){
                if(version == emoji.version){
                    return true;
                }
            }
        }
        return false;
    }
}
