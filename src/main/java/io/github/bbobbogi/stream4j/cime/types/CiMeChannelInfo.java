package io.github.bbobbogi.stream4j.cime.types;

import io.github.bbobbogi.stream4j.common.ChannelInfo;

/**
 * Channel metadata for CiMe.
 *
 * @since 1.0.0
 */
public class CiMeChannelInfo implements ChannelInfo {
    int id;
    String slug;
    String name;
    String description;
    int followerCount;
    int subscriberCount;
    boolean isLive;
    int level;

    /**
     * Returns the channel ID as a string.
     *
     * @return channel ID
     */
    @Override
    public String getId() {
        return String.valueOf(id);
    }

    /**
     * Returns the channel name.
     *
     * @return channel name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns whether the channel is currently live.
     *
     * @return {@code true} if live, otherwise {@code false}
     */
    @Override
    public boolean isLive() {
        return isLive;
    }

    /**
     * Returns the numeric channel ID.
     *
     * @return channel ID as integer
     */
    public int getIdAsInt() {
        return id;
    }

    /**
     * Returns the channel slug.
     *
     * @return channel slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Returns the channel description.
     *
     * @return channel description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns follower count.
     *
     * @return follower count
     */
    public int getFollowerCount() {
        return followerCount;
    }

    /**
     * Returns subscriber count.
     *
     * @return subscriber count
     */
    public int getSubscriberCount() {
        return subscriberCount;
    }

    /**
     * Returns channel level.
     *
     * @return level value
     */
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
