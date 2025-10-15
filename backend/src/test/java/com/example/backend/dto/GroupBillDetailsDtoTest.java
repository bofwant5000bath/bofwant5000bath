package com.example.backend.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GroupBillDetailsDtoTest {

    @Mock
    private UserDto mockUserDto;
    @Mock
    private BillDetailDto mockBillDetailDto;

    @Test
    void constructor_ShouldStoreListsCorrectly() {
        // Arrange
        List<UserDto> members = Collections.singletonList(mockUserDto);
        List<BillDetailDto> bills = Collections.singletonList(mockBillDetailDto);

        // Act
        GroupBillDetailsDto dto = new GroupBillDetailsDto(members, bills);

        // Assert
        assertNotNull(dto.getGroupMembers());
        assertNotNull(dto.getBills());
        assertEquals(1, dto.getGroupMembers().size());
        assertEquals(1, dto.getBills().size());
        assertEquals(members, dto.getGroupMembers());
        assertEquals(bills, dto.getBills());
    }
}