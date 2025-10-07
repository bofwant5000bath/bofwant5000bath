package com.example.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO class to represent a comprehensive dashboard summary for a user.
 */
public class DashboardSummaryDto {
    private BigDecimal totalOwed;
    private BigDecimal totalReceivable;
    private List<GroupDetailsDto> groups;

    public DashboardSummaryDto(BigDecimal totalOwed, BigDecimal totalReceivable, List<GroupDetailsDto> groups) {
        this.totalOwed = totalOwed;
        this.totalReceivable = totalReceivable;
        this.groups = groups;
    }

    public BigDecimal getTotalOwed() {
        return totalOwed;
    }

    public void setTotalOwed(BigDecimal totalOwed) {
        this.totalOwed = totalOwed;
    }

    public BigDecimal getTotalReceivable() {
        return totalReceivable;
    }

    public void setTotalReceivable(BigDecimal totalReceivable) {
        this.totalReceivable = totalReceivable;
    }

    public List<GroupDetailsDto> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDetailsDto> groups) {
        this.groups = groups;
    }
}
