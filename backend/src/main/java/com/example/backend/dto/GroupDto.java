package com.example.backend.dto;

import com.example.backend.model.Group;
import com.example.backend.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupDto {
    private Integer groupId;
    private String groupName;
    private UserDto createdByUser;
    private String createdAt;

    public GroupDto(Group group) {
        this.groupId = group.getGroupId();
        this.groupName = group.getGroupName();
        this.createdByUser = new UserDto(group.getCreatedByUser());
        this.createdAt = group.getCreatedAt() != null ? group.getCreatedAt().toString() : null;
    }
}