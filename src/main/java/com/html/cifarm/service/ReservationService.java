package com.html.cifarm.service;

import com.html.cifarm.domain.*;
import com.html.cifarm.dto.response.ReservationResponseDto;
import com.html.cifarm.repository.FarmSlotRepository;
import com.html.cifarm.repository.FarmUserRepository;
import com.html.cifarm.repository.ReservationRepository;
import com.html.cifarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final FarmSlotRepository farmSlotRepository;
    private final ReservationRepository reservationRepository;
    private final FarmUserRepository farmUserRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReservationResponseDto selectSlots(Long farmId, List<Integer> slotNumbers, Long userId) {

        // 지정된 농장에서 선택된 슬롯 번호로 슬롯 조회
        List<FarmSlot> slots = farmSlotRepository.findByFarmIdAndSlotNumberIn(farmId, slotNumbers);

        if (slots.isEmpty()) {
            throw new RuntimeException("선택한 구획이 없습니다.");
        }

        int totalSlots = slots.size();
        int totalArea = totalSlots * 7; // 면적 계산
        int totalPrice = slots.stream().mapToInt(FarmSlot::getSlotPrice).sum();

        // userId로 User 객체 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // FarmUser를 기존에 저장된 사용자로 조회
        List<FarmUser> farmUsers = farmUserRepository.findByUser(user);
        if (farmUsers.isEmpty()) {
            throw new RuntimeException("해당 사용자에 대한 FarmUser를 찾을 수 없습니다.");
        }

        // 가장 최근의 FarmUser 선택 (createdAt 기준으로)
        FarmUser farmUser = farmUsers.stream()
                .max(Comparator.comparing(FarmUser::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("최근 FarmUser를 찾을 수 없습니다."));

        // 선택한 모든 슬롯에 대해 예약 생성
        for (Integer slotNumber : slotNumbers) {
            Reservation reservation = Reservation.builder()
                    .farmUser(farmUser) // 기존 FarmUser 객체 사용
                    .slotId(slotNumber) // 선택한 슬롯 ID 저장
                    // 날짜는 나중에 날짜 선택 API에서 처리
                    .build();

            reservationRepository.save(reservation);
        }

        return new ReservationResponseDto(totalSlots, totalArea, totalPrice);
    }

    public int calculateTotalPrice(Long farmId, List<Integer> slotNumbers, LocalDate rentalStartDate, LocalDate rentalEndDate) {
        // 해당 농장에서 선택된 슬롯 번호로 슬롯 조회
        List<FarmSlot> slots = farmSlotRepository.findByFarmIdAndSlotNumberIn(farmId, slotNumbers);

        if (slots.isEmpty()) {
            throw new RuntimeException("선택한 슬롯이 농장에 존재하지 않습니다.");
        }

        // 총 금액 계산 (예를 들어, 대여 기간에 따라 가격을 조정할 수 있음)
        int totalPrice = slots.stream().mapToInt(FarmSlot::getSlotPrice).sum();
        int rentalDays = (int) ChronoUnit.DAYS.between(rentalStartDate, rentalEndDate) + 1; // 대여일 수 계산

        return totalPrice * rentalDays; // 총 금액 반환
    }

}
