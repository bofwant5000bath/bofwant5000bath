package com.example.backend.dto;

import java.util.List;

public class AddMembersRequestDto {

    private List<Integer> memberIds;

    // Getter
    public List<Integer> getMemberIds() {
        return memberIds;
    }

    // Setter
    public void setMemberIds(List<Integer> memberIds) {
        this.memberIds = memberIds;
    }
}