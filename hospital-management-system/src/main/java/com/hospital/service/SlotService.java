package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.DoctorSlot;
import com.hospital.entity.DoctorSlot.SlotStatus;
import com.hospital.entity.DoctorSlot.SlotType;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final DoctorSlotRepository slotRepo;
    private final DoctorRepository     doctorRepo;

    /** All active slots for a doctor */
    public List<DoctorSlot> getDoctorSlots(Long doctorId) {
        return slotRepo.findByDoctorIdAndIsActiveOrderByStartTime(doctorId, true);
    }

    /** Available slots for a doctor on a given date */
    public List<DoctorSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        return slotRepo.findAvailableSlots(doctorId, date, date.getDayOfWeek());
    }

    /** Calendar week view: slots from monday to sunday */
    public Map<LocalDate, List<DoctorSlot>> getWeekCalendar(Long doctorId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<DoctorSlot> slots = slotRepo.findSlotsForWeek(doctorId, weekStart, weekEnd);

        Map<LocalDate, List<DoctorSlot>> calendar = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            DayOfWeek dow = day.getDayOfWeek();

            List<DoctorSlot> daySlots = new ArrayList<>();
            for (DoctorSlot s : slots) {
                boolean match = (s.getSlotType() == SlotType.SPECIFIC_DATE && day.equals(s.getSlotDate()))
                             || (s.getSlotType() == SlotType.RECURRING && dow == s.getDayOfWeek());
                if (match) daySlots.add(s);
            }
            daySlots.sort(Comparator.comparing(DoctorSlot::getStartTime));
            calendar.put(day, daySlots);
        }
        return calendar;
    }

    /** Create a specific-date slot */
    @Transactional
    public DoctorSlot createSpecificSlot(Long doctorId, LocalDate date,
                                         LocalTime start, LocalTime end,
                                         int duration, int maxPatients) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        if (slotRepo.existsByDoctorIdAndSlotDateAndStartTime(doctorId, date, start))
            throw new BadRequestException("A slot already exists at this time.");

        return slotRepo.save(DoctorSlot.builder()
            .doctor(doctor)
            .slotDate(date)
            .startTime(start)
            .endTime(end)
            .slotDurationMinutes(duration)
            .maxPatients(maxPatients)
            .slotType(SlotType.SPECIFIC_DATE)
            .status(SlotStatus.AVAILABLE)
            .isActive(true)
            .build());
    }

    /** Create a recurring weekly slot */
    @Transactional
    public DoctorSlot createRecurringSlot(Long doctorId, DayOfWeek day,
                                          LocalTime start, LocalTime end,
                                          int duration, int maxPatients) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        return slotRepo.save(DoctorSlot.builder()
            .doctor(doctor)
            .dayOfWeek(day)
            .startTime(start)
            .endTime(end)
            .slotDurationMinutes(duration)
            .maxPatients(maxPatients)
            .slotType(SlotType.RECURRING)
            .status(SlotStatus.AVAILABLE)
            .isActive(true)
            .build());
    }

    /** Block a slot with reason */
    @Transactional
    public void blockSlot(Long slotId, String reason) {
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", slotId));
        slot.setStatus(SlotStatus.BLOCKED);
        slot.setBlockReason(reason);
        slotRepo.save(slot);
    }

    /** Unblock / restore a slot */
    @Transactional
    public void unblockSlot(Long slotId) {
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", slotId));
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setBlockReason(null);
        slotRepo.save(slot);
    }

    /** Soft-delete a slot */
    @Transactional
    public void deleteSlot(Long slotId) {
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", slotId));
        slot.setIsActive(false);
        slotRepo.save(slot);
    }
}
