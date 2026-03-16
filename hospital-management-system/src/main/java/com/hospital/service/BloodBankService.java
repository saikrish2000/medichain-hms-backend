package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.BloodRequest.*;
import com.hospital.enums.BloodGroup;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class BloodBankService {

    private final BloodBankRepository      bankRepo;
    private final BloodInventoryRepository inventoryRepo;
    private final BloodRequestRepository   requestRepo;
    private final BloodDonationRepository  donationRepo;
    private final UserRepository           userRepo;

    private static final AtomicLong SEQ = new AtomicLong(1000);

    // ── INVENTORY ─────────────────────────────────────────

    public List<BloodInventory> getInventory(Long bankId) {
        return inventoryRepo.findByBloodBankIdOrderByBloodGroup(bankId);
    }

    public List<BloodInventory> getAllInventory() {
        return inventoryRepo.findAllActive();
    }

    public List<BloodInventory> getLowStockAlerts() {
        return inventoryRepo.findLowStock();
    }

    @Transactional
    public BloodInventory updateStock(Long bankId, BloodGroup group, int units) {
        BloodBank bank = bankRepo.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank", "id", bankId));
        BloodInventory inv = inventoryRepo
            .findByBloodBankIdAndBloodGroup(bankId, group)
            .orElseGet(() -> BloodInventory.builder()
                .bloodBank(bank).bloodGroup(group).unitsAvailable(0).unitsReserved(0).build());
        inv.setUnitsAvailable(Math.max(0, inv.getUnitsAvailable() + units));
        return inventoryRepo.save(inv);
    }

    @Transactional
    public BloodInventory setStock(Long bankId, BloodGroup group, int units, int threshold) {
        BloodBank bank = bankRepo.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank", "id", bankId));
        BloodInventory inv = inventoryRepo
            .findByBloodBankIdAndBloodGroup(bankId, group)
            .orElseGet(() -> BloodInventory.builder()
                .bloodBank(bank).bloodGroup(group).build());
        inv.setUnitsAvailable(units);
        inv.setMinimumThreshold(threshold);
        return inventoryRepo.save(inv);
    }

    // ── REQUESTS ──────────────────────────────────────────

    @Transactional
    public BloodRequest createRequest(Long bankId, Long userId,
                                       BloodGroup group, int units,
                                       String patientName, String reason,
                                       String phone, boolean isEmergency,
                                       RequesterType requesterType) {
        BloodBank bank = bankRepo.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank", "id", bankId));
        User user = userId != null ? userRepo.findById(userId).orElse(null) : null;

        // Check availability
        BloodInventory inv = inventoryRepo
            .findByBloodBankIdAndBloodGroup(bankId, group)
            .orElseThrow(() -> new BadRequestException("Blood group " + group + " not found in inventory."));

        if (inv.getUnitsUsable() < units)
            throw new BadRequestException("Insufficient units. Available: " + inv.getUnitsUsable());

        String num = "BLR-" + LocalDate.now().getYear() + "-"
                   + String.format("%05d", SEQ.getAndIncrement());

        BloodRequest req = BloodRequest.builder()
            .requestNumber(num).bloodBank(bank).requestedBy(user)
            .bloodGroup(group).unitsRequested(units)
            .patientName(patientName).reason(reason)
            .contactPhone(phone).isEmergency(isEmergency)
            .requesterType(requesterType).status(RequestStatus.PENDING)
            .build();

        // Reserve units
        inv.setUnitsReserved(inv.getUnitsReserved() + units);
        inventoryRepo.save(inv);
        return requestRepo.save(req);
    }

    @Transactional
    public void approveRequest(Long requestId, int unitsApproved, Long reviewerUserId) {
        BloodRequest req = getRequest(requestId);
        User reviewer = userRepo.findById(reviewerUserId).orElse(null);

        req.setStatus(RequestStatus.APPROVED);
        req.setUnitsApproved(unitsApproved);
        req.setReviewedBy(reviewer);
        req.setReviewedAt(java.time.LocalDateTime.now());

        // Deduct from inventory
        inventoryRepo.findByBloodBankIdAndBloodGroup(req.getBloodBank().getId(), req.getBloodGroup())
            .ifPresent(inv -> {
                inv.setUnitsAvailable(inv.getUnitsAvailable() - unitsApproved);
                inv.setUnitsReserved(Math.max(0, inv.getUnitsReserved() - req.getUnitsRequested()));
                inventoryRepo.save(inv);
            });
        requestRepo.save(req);
    }

    @Transactional
    public void rejectRequest(Long requestId, String reason, Long reviewerUserId) {
        BloodRequest req = getRequest(requestId);
        req.setStatus(RequestStatus.REJECTED);
        req.setRejectionReason(reason);
        req.setReviewedAt(java.time.LocalDateTime.now());
        // Release reserved units
        inventoryRepo.findByBloodBankIdAndBloodGroup(req.getBloodBank().getId(), req.getBloodGroup())
            .ifPresent(inv -> {
                inv.setUnitsReserved(Math.max(0, inv.getUnitsReserved() - req.getUnitsRequested()));
                inventoryRepo.save(inv);
            });
        requestRepo.save(req);
    }

    // ── DONATIONS ─────────────────────────────────────────

    @Transactional
    public BloodDonation registerDonation(Long bankId, Long donorUserId,
                                           BloodGroup group, int units) {
        BloodBank bank = bankRepo.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank", "id", bankId));
        User donor = userRepo.findById(donorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", donorUserId));

        BloodDonation donation = BloodDonation.builder()
            .bloodBank(bank).donor(donor)
            .bloodGroup(group).unitsDonated(units)
            .donationDate(LocalDate.now())
            .nextEligibleDate(LocalDate.now().plusDays(90))
            .status(BloodDonation.DonationStatus.PENDING)
            .build();
        return donationRepo.save(donation);
    }

    @Transactional
    public void acceptDonation(Long donationId, Long screenerId) {
        BloodDonation d = donationRepo.findById(donationId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodDonation", "id", donationId));
        d.setStatus(BloodDonation.DonationStatus.ACCEPTED);
        d.setScreenedByUserId(screenerId);
        donationRepo.save(d);
        // Add to inventory
        updateStock(d.getBloodBank().getId(), d.getBloodGroup(), d.getUnitsDonated());
    }

    // ── QUERIES ───────────────────────────────────────────

    public Page<BloodRequest> getPendingRequests(Long bankId, int page) {
        return requestRepo.findByBloodBankIdAndStatus(bankId, RequestStatus.PENDING)
            .stream().collect(java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toList(),
                list -> new PageImpl<>(list, PageRequest.of(page, 15), list.size())));
    }

    public Page<BloodRequest> getAllRequests(Long bankId, int page) {
        return requestRepo.findByBloodBankId(bankId, PageRequest.of(page, 15,
            Sort.by("createdAt").descending()));
    }

    public BloodRequest getRequest(Long id) {
        return requestRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", "id", id));
    }

    public Map<String, Long> getDashboardStats(Long bankId) {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("pending",   requestRepo.countByBloodBankIdAndStatus(bankId, RequestStatus.PENDING));
        stats.put("approved",  requestRepo.countByBloodBankIdAndStatus(bankId, RequestStatus.APPROVED));
        stats.put("donations", donationRepo.countByBloodBankIdAndStatus(bankId, BloodDonation.DonationStatus.PENDING));
        stats.put("lowStock",  (long) inventoryRepo.findLowStock().size());
        return stats;
    }
}
