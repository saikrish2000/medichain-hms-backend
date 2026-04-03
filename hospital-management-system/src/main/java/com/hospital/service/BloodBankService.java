package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.enums.BloodGroup;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BloodBankService {

    private final BloodBankRepository      bankRepo;
    private final BloodInventoryRepository inventoryRepo;
    private final BloodRequestRepository   requestRepo;
    private final BloodDonationRepository  donationRepo;
    private final UserRepository           userRepo;

    public Map<String,Object> getDashboardStats() {
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("totalUnits",     inventoryRepo.sumTotalUnits().orElse(0L));
        s.put("pendingRequests",requestRepo.countByStatus(BloodRequest.Request"PENDING"));
        s.put("inventory",      inventoryRepo.findAll());
        return s;
    }

    public List<BloodInventory> getAllInventory() { return inventoryRepo.findAll(); }

    @Transactional
    public BloodInventory setStock(Long bankId, BloodGroup group, int units, int threshold) {
        BloodInventory inv = inventoryRepo.findByBloodBankIdAndBloodGroup(bankId, group)
            .orElseGet(() -> {
                BloodInventory i = new BloodInventory();
                BloodBank bank = bankRepo.findById(bankId)
                    .orElseThrow(() -> new ResourceNotFoundException("BloodBank","id",bankId));
                i.setBloodBank(bank); i.setBloodGroup(group); return i;
            });
        inv.setUnitsAvailable(units);
        inv.setMinimumThreshold(threshold);
        return inventoryRepo.save(inv);
    }

    @Transactional
    public BloodInventory updateStock(Long bankId, BloodGroup group, int units) {
        BloodInventory inv = inventoryRepo.findByBloodBankIdAndBloodGroup(bankId, group)
            .orElseGet(() -> {
                BloodInventory i = new BloodInventory();
                BloodBank bank = bankRepo.findById(bankId)
                    .orElseThrow(() -> new ResourceNotFoundException("BloodBank","id",bankId));
                i.setBloodBank(bank); i.setBloodGroup(group); i.setUnitsAvailable(0); return i;
            });
        inv.setUnitsAvailable(inv.getUnitsAvailable() + units);
        return inventoryRepo.save(inv);
    }

    @Transactional
    public BloodRequest createRequest(Long bankId, Long userId, BloodGroup group,
                                      int units, String urgency, String reason) {
        BloodBank bank = bankRepo.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank","id",bankId));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User","id",userId));
        BloodRequest req = new BloodRequest();
        req.setBank(bank); req.setRequestedBy(user); req.setBloodGroup(group);
        req.setUnitsRequested(units); req.setUrgencyLevel(urgency); req.setReason(reason);
        req.setStatus(BloodRequest.Request"PENDING");
        req.setCreatedAt(LocalDateTime.now());
        return requestRepo.save(req);
    }

    @Transactional
    public void approveRequest(Long requestId, int unitsApproved, Long reviewerUserId) {
        BloodRequest req = requestRepo.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodRequest","id",requestId));
        BloodInventory inv = inventoryRepo.findByBloodBankIdAndBloodGroup(
            req.getBank().getId(), req.getBloodGroup())
            .orElseThrow(() -> new BadRequestException("No inventory found"));
        if (inv.getUnitsAvailable() < unitsApproved)
            throw new BadRequestException("Insufficient stock: " + inv.getUnitsAvailable() + " units");
        inv.setUnitsAvailable(inv.getUnitsAvailable() - unitsApproved);
        inventoryRepo.save(inv);
        req.setStatus(BloodRequest.RequestStatus.APPROVED);
        req.setUnitsApproved(unitsApproved);
        userRepo.findById(reviewerUserId).ifPresent(req::setReviewedBy);
        req.setReviewedAt(LocalDateTime.now());
        requestRepo.save(req);
    }

    @Transactional
    public void rejectRequest(Long requestId, String reason, Long reviewerUserId) {
        BloodRequest req = requestRepo.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodRequest","id",requestId));
        req.setStatus(BloodRequest.RequestStatus.REJECTED);
        req.setRejectionReason(reason);
        userRepo.findById(reviewerUserId).ifPresent(req::setReviewedBy);
        req.setReviewedAt(LocalDateTime.now());
        requestRepo.save(req);
    }

    @Transactional
    public BloodDonation registerDonation(Long bankId, Long donorUserId, BloodGroup group, int units) {
        BloodBank bank = bankRepo.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank","id",bankId));
        User donor = userRepo.findById(donorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User","id",donorUserId));
        BloodDonation d = new BloodDonation();
        d.setBank(bank); d.setDonor(donor); d.setBloodGroup(group);
        d.setUnitsCollected(units); d.setStatus(BloodDonation.Donation"PENDING");
        d.setDonationDate(java.time.LocalDate.now());
        return donationRepo.save(d);
    }

    @Transactional
    public void acceptDonation(Long donationId, Long screenerId) {
        BloodDonation donation = donationRepo.findById(donationId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodDonation","id",donationId));
        donation.setStatus(BloodDonation.DonationStatus.ACCEPTED);
        donationRepo.save(donation);
        updateStock(donation.getBank().getId(), donation.getBloodGroup(), donation.getUnitsCollected());
    }

    public List<BloodDonation> getAllDonations() {
        return donationRepo.findAll();
    }
}
