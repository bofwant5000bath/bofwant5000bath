package com.example.backend.controller;

import com.example.backend.dto.BillDetailDto;
import com.example.backend.dto.CreateBillRequest;
import com.example.backend.dto.CreateBillWithTagsRequest;
import com.example.backend.model.Bill;
import com.example.backend.model.BillParticipant;
import com.example.backend.service.BillService;
import com.example.backend.repository.BillParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;
    private final BillParticipantRepository billParticipantRepository;

    @Autowired
    public BillController(BillService billService,
                          BillParticipantRepository billParticipantRepository) {
        this.billService = billService;
        this.billParticipantRepository = billParticipantRepository;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<BillDetailDto>> getBillsByGroup(@PathVariable Integer groupId) {
        List<Bill> bills = billService.getBillsByGroupId(groupId);

        List<BillDetailDto> billDetailDtos = bills.stream()
                .map(bill -> {
                    List<BillParticipant> participants =
                            billParticipantRepository.findByBillBillId(bill.getBillId());
                    return new BillDetailDto(bill, participants);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(billDetailDtos);
    }

    @PostMapping("/create")
    public ResponseEntity<BillDetailDto> createBill(@RequestBody CreateBillRequest request) {
        try {
            Bill createdBill = billService.createBill(request);
            List<BillParticipant> participants =
                    billParticipantRepository.findByBillBillId(createdBill.getBillId());
            return new ResponseEntity<>(new BillDetailDto(createdBill, participants), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/create/by-tag")
    public ResponseEntity<BillDetailDto> createBillByTag(@RequestBody CreateBillWithTagsRequest request) {
        try {
            Bill createdBill = billService.createBillByTag(request);
            List<BillParticipant> participants =
                    billParticipantRepository.findByBillBillId(createdBill.getBillId());
            return new ResponseEntity<>(new BillDetailDto(createdBill, participants), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
