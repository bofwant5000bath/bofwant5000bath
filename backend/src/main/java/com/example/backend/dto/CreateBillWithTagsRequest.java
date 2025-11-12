package com.example.backend.dto;

import com.example.backend.model.SplitMethod;  // ✅ ใช้ enum ไฟล์แยก
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateBillWithTagsRequest {
    private Integer groupId;
    private String title;
    private String description;
    private BigDecimal amount;
    private Integer paidByUserId;
    private SplitMethod splitMethod;
    private List<TagSplitDto> tags;
    // ✅ ---- START: เพิ่มฟิลด์ใหม่ 4 บรรทัดนี้ ----
    private String currencyCode;
    private BigDecimal exchangeRate;
    private String promptpayNumber;
    private String receiptImageUrl;
    // ✅ ---- END: เพิ่มฟิลด์ใหม่ ----

    @Getter
    @Setter
    public static class TagSplitDto {
        private String tagName;
        private BigDecimal tagAmount;
        private List<ParticipantSplitDto> participants;
    }

    @Getter
    @Setter
    public static class ParticipantSplitDto {
        private Integer userId;
        private BigDecimal amount;
    }
}
