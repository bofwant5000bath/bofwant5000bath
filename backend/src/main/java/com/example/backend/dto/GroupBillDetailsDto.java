package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupBillDetailsDto {

    private List<UserDto> groupMembers;
    private List<BillDetailDto> bills;

    public GroupBillDetailsDto(List<UserDto> groupMembers, List<BillDetailDto> bills) {
        this.groupMembers = groupMembers;
        this.bills = bills;
    }
}