package guesthouse.guestHousePost.dto;

import guesthouse.guestHousePost.domain.model.Party;
import guesthouse.guestHousePost.domain.vo.DayOfWeek;
import guesthouse.guestHousePost.domain.vo.PartyType;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Builder
public record PartyWithImageUrlDTO(
        Long guestHousePostId,
        PartyType type,
        String otherPartyType,
        LocalTime startTime,
        LocalTime endTime,
        Set<DayOfWeek> weeklyDays,
        String place,
        String moods,
        Boolean isExternalGuestAllowed,
        Long guestFee,
        Long externalGuestFee,
        String information,
        List<String> imageUrls
) {
    public static PartyWithImageUrlDTO from(Party party, List<String> imageUrls) {
        return PartyWithImageUrlDTO.builder()
                .guestHousePostId(party.getGuestHousePostId())
                .type(party.getPartyType())
                .otherPartyType(party.getOtherPartyType())
                .startTime(party.getStartTime())
                .endTime(party.getEndTime())
                .weeklyDays(party.getWeeklyDays())
                .place(party.getPlace())
                .moods(party.getMoods())
                .isExternalGuestAllowed(party.getIsExternalGuestAllowed())
                .guestFee(party.getGuestFee())
                .externalGuestFee(party.getExternalGuestFee())
                .information(party.getInformation())
                .imageUrls(imageUrls)
                .build();
    }
}
