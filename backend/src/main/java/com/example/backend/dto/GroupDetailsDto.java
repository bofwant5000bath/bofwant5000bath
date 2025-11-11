package com.example.backend.dto;

import com.example.backend.model.Group;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * DTO class for detailed group information on the dashboard.
 */
public class GroupDetailsDto {
    private Integer groupId;
    private String groupName;
    private Integer memberCount;
    private BigDecimal groupTotalAmount;
    private BigDecimal myDebt;
    private BigDecimal othersDebtToMe;
    private boolean isPinned;

    public GroupDetailsDto(Group group, Integer memberCount, BigDecimal groupTotalAmount, BigDecimal myDebt, BigDecimal othersDebtToMe, boolean isPinned) {
        this.groupId = group.getGroupId();
        this.groupName = group.getGroupName();
        this.memberCount = Optional.ofNullable(memberCount).orElse(0);
        this.groupTotalAmount = Optional.ofNullable(groupTotalAmount).orElse(BigDecimal.ZERO);
        this.myDebt = Optional.ofNullable(myDebt).orElse(BigDecimal.ZERO);
        this.othersDebtToMe = Optional.ofNullable(othersDebtToMe).orElse(BigDecimal.ZERO);
        this.isPinned = isPinned;
    }

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

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public BigDecimal getGroupTotalAmount() {
        return groupTotalAmount;
    }

    public void setGroupTotalAmount(BigDecimal groupTotalAmount) {
        this.groupTotalAmount = groupTotalAmount;
    }

    public BigDecimal getMyDebt() {
        return myDebt;
    }

    public void setMyDebt(BigDecimal myDebt) {
        this.myDebt = myDebt;
    }

    public BigDecimal getOthersDebtToMe() {
        return othersDebtToMe;
    }

    public void setOthersDebtToMe(BigDecimal othersDebtToMe) {
        this.othersDebtToMe = othersDebtToMe;
    }

    public boolean isPinned() { return isPinned; }
}
