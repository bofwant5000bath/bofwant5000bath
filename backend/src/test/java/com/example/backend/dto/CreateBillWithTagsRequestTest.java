package com.example.backend.dto;

import com.example.backend.model.SplitMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CreateBillWithTagsRequestTest {

    @Mock
    private SplitMethod mockSplitMethod;

    @Test
    void testGettersAndSetters() {
        // Arrange
        CreateBillWithTagsRequest request = new CreateBillWithTagsRequest();

        // สร้าง Participant DTO (ชั้นในสุด)
        CreateBillWithTagsRequest.ParticipantSplitDto participant = new CreateBillWithTagsRequest.ParticipantSplitDto();
        participant.setUserId(2);
        participant.setAmount(new BigDecimal("250.00"));

        // สร้าง Tag DTO (ชั้นกลาง)
        CreateBillWithTagsRequest.TagSplitDto tag = new CreateBillWithTagsRequest.TagSplitDto();
        tag.setTagName("Food");
        tag.setTagAmount(new BigDecimal("500.00"));
        tag.setParticipants(Collections.singletonList(participant));

        List<CreateBillWithTagsRequest.TagSplitDto> tags = Collections.singletonList(tag);

        // Act
        request.setGroupId(1);
        request.setTitle("Shopping Day");
        request.setDescription("Groceries and snacks");
        request.setAmount(new BigDecimal("1000.00"));
        request.setPaidByUserId(1);
        request.setSplitMethod(mockSplitMethod);
        request.setTags(tags);

        // Assert
        assertEquals(1, request.getGroupId());
        assertEquals("Shopping Day", request.getTitle());
        assertEquals(0, new BigDecimal("1000.00").compareTo(request.getAmount()));
        assertEquals(1, request.getPaidByUserId());
        assertEquals(mockSplitMethod, request.getSplitMethod());

        // Assert Tag DTO
        assertNotNull(request.getTags());
        assertEquals(1, request.getTags().size());
        assertEquals("Food", request.getTags().get(0).getTagName());
        assertEquals(0, new BigDecimal("500.00").compareTo(request.getTags().get(0).getTagAmount()));

        // Assert Participant DTO
        assertNotNull(request.getTags().get(0).getParticipants());
        assertEquals(1, request.getTags().get(0).getParticipants().size());
        assertEquals(2, request.getTags().get(0).getParticipants().get(0).getUserId());
        assertEquals(0, new BigDecimal("250.00").compareTo(request.getTags().get(0).getParticipants().get(0).getAmount()));
    }
}