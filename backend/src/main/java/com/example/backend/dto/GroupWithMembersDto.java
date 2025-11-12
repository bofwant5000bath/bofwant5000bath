package com.example.backend.dto;

import java.util.List;

// DTO นี้จะใช้เก็บข้อมูล 1 กลุ่ม (พร้อมสมาชิก) ในลิสต์ที่จะส่งกลับไป
public class GroupWithMembersDto {

    private Integer groupId;
    private String groupName;
    private boolean isPinned;
    private List<MemberDto> members; // เราจะใช้ MemberDto ที่เคยสร้างไว้

    // Getters and Setters
    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public List<MemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<MemberDto> members) {
        this.members = members;
    }
}