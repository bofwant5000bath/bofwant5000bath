package com.example.backend.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "pinned_groups")
public class PinnedGroup implements Serializable {

    @EmbeddedId
    private PinnedGroupId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // แมปกับฟิลด์ 'userId' ใน PinnedGroupId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId") // แมปกับฟิลด์ 'groupId' ใน PinnedGroupId
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "is_pinned")
    private Boolean isPinned = true;

    // Constructors
    public PinnedGroup() {
    }

    public PinnedGroup(User user, Group group) {
        this.user = user;
        this.group = group;
        this.id = new PinnedGroupId(user.getUserId(), group.getGroupId());
        this.isPinned = true;
    }

    // Getters and Setters
    public PinnedGroupId getId() {
        return id;
    }

    public void setId(PinnedGroupId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Boolean getPinned() {
        return isPinned;
    }

    public void setPinned(Boolean pinned) {
        isPinned = pinned;
    }
}