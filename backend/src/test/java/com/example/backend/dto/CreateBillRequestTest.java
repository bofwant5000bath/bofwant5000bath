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
class CreateBillRequestTest {

    @Mock
    private SplitMethod mockSplitMethod; // Mock enum ที่อาจจะยังไม่มี

    @Test
    void testGettersAndSetters() {
        // Arrange
        CreateBillRequest request = new CreateBillRequest();
        BigDecimal billAmount = new BigDecimal("1200.00");
        List<Integer> selectedIds = Collections.singletonList(2);

        // สร้างและตั้งค่า DTO ย่อย
        CreateBillRequest.ParticipantSplitDto participantDto = new CreateBillRequest.ParticipantSplitDto();
        participantDto.setUserId(2);
        participantDto.setAmount(new BigDecimal("600.00"));
        List<CreateBillRequest.ParticipantSplitDto> participants = Collections.singletonList(participantDto);

        // Act
        request.setGroupId(1);
        request.setTitle("Hotel Booking");
        request.setDescription("2 nights stay");
        request.setAmount(billAmount);
        request.setPaidByUserId(1);
        request.setSplitMethod(mockSplitMethod); // ใช้ Mock
        request.setParticipants(participants);
        request.setSelectedParticipantIds(selectedIds);


        // Assert
        assertEquals(1, request.getGroupId());
        assertEquals("Hotel Booking", request.getTitle());
        assertEquals("2 nights stay", request.getDescription());
        assertEquals(0, billAmount.compareTo(request.getAmount()));
        assertEquals(1, request.getPaidByUserId());
        assertEquals(mockSplitMethod, request.getSplitMethod());
        assertEquals(selectedIds, request.getSelectedParticipantIds());

        // Assert DTO ย่อย
        assertNotNull(request.getParticipants());
        assertEquals(1, request.getParticipants().size());
        assertEquals(2, request.getParticipants().get(0).getUserId());
        assertEquals(0, new BigDecimal("600.00").compareTo(request.getParticipants().get(0).getAmount()));
    }
}