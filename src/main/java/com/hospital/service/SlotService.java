package com.hospital.service;

import com.hospital.entity.DoctorSlot;
import com.hospital.entity.Doctor;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final DoctorSlotRepository slotRepo;
    private final DoctorRepository     doctorRepo;

    public List<DoctorSlot> getDoctorSlots(Long doctorId) {
        return slotRepo.findByDoctorId(doctorId);
    }

    /**
     * Create individual time-slots by splitting a time range for a specific date.
     */
    @Transactional
    public void createSpecificSlot(Long doctorId, LocalDate date,
                                   LocalTime start, LocalTime end,
                                   int durationMin, int maxPatients) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        LocalTime cursor = start;
        while (cursor.plusMinutes(durationMin).compareTo(end) <= 0) {
            DoctorSlot slot = new DoctorSlot();
            slot.setDoctor(doctor);
            slot.setSlotDate(date);
            slot.setStartTime(cursor);
            slot.setEndTime(cursor.plusMinutes(durationMin));
            slot.setDurationMinutes(durationMin);
            slot.setMaxPatients(maxPatients);
            slot.setCurrentPatients(0);
            slot.setIsBlocked(false);
            slotRepo.save(slot);
            cursor = cursor.plusMinutes(durationMin);
        }
    }

    /**
     * Create a single slot from a pre-filled form object.
     */
    @Transactional
    public DoctorSlot createSpecificSlot(Long doctorId, DoctorSlot form) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        form.setDoctor(doctor);
        form.setCurrentPatients(0);
        form.setIsBlocked(false);
        return slotRepo.save(form);
    }

    /**
     * Create a recurring slot entry. dayOfWeek is stored as String (e.g. "MONDAY").
     */
    @Transactional
    public DoctorSlot createRecurringSlot(Long doctorId, DayOfWeek day,
                                          LocalTime start, LocalTime end,
                                          int durationMin, int maxPatients,
                                          LocalDate forDate) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        DoctorSlot slot = new DoctorSlot();
        slot.setDoctor(doctor);
        slot.setDayOfWeek(day.name());          // entity field is String
        slot.setSlotDate(forDate);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setDurationMinutes(durationMin);
        slot.setMaxPatients(maxPatients);
        slot.setCurrentPatients(0);
        slot.setIsBlocked(false);
        slot.setIsRecurring(true);
        return slotRepo.save(slot);
    }

    /**
     * Generate concrete slots for every occurrence of a weekday over N weeks ahead.
     */
    @Transactional
    public void createRecurringSlots(Long doctorId, DayOfWeek day,
                                     LocalTime start, LocalTime end,
                                     int durationMin, int maxPatients, int weeksAhead) {
        LocalDate cursor = LocalDate.now();
        LocalDate limit  = cursor.plusWeeks(weeksAhead);
        while (!cursor.isAfter(limit)) {
            if (cursor.getDayOfWeek() == day) {
                createSpecificSlot(doctorId, cursor, start, end, durationMin, maxPatients);
            }
            cursor = cursor.plusDays(1);
        }
    }

    @Transactional
    public void toggleSlotBlock(Long slotId) {
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", slotId));
        slot.setIsBlocked(!slot.getIsBlocked());
        slotRepo.save(slot);
    }

    @Transactional
    public void blockSlot(Long slotId, String reason) {
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", slotId));
        slot.setIsBlocked(true);
        slot.setBlockReason(reason);
        slotRepo.save(slot);
    }

    @Transactional
    public void unblockSlot(Long slotId) {
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", slotId));
        slot.setIsBlocked(false);
        slot.setBlockReason(null);
        slotRepo.save(slot);
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        slotRepo.deleteById(slotId);
    }
}
