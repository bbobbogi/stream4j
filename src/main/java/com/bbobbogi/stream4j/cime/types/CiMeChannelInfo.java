package com.bbobbogi.stream4j.cime.types;

import com.bbobbogi.stream4j.common.ChannelInfo;

public class CiMeChannelInfo implements ChannelInfo {
    int id;
    String slug;
    String name;
    String description;
    int followerCount;
    int subscriberCount;
    boolean isLive;
    int level;

    @Override
    public String getId() {
        return String.valueOf(id);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLive() {
        return isLive;
    }

    public int getIdAsInt() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "CiMeChannelInfo{" +
                "id=" + id +
                ", slug='" + slug + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", followerCount=" + followerCount +
                ", subscriberCount=" + subscriberCount +
                ", isLive=" + isLive +
                ", level=" + level +
                '}';
    }
}
