package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.Party;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class PartyMapper {

    public static final String DELIMITER = "|:|";

    public static Party toParty(GuestHouseCreateRequest.Party party, Long guestHousePostId) {
        return Party.builder()
                .guestHousePostId(guestHousePostId)
                .partyType(party.type())
                .otherPartyType(party.otherPartyType())
                .startTime(party.startTime())
                .endTime(party.endTime())
                .weeklyDays(party.weeklyDays())
                .place(party.place())
                .moods(createMoods(party.moods()))
                .isExternalGuestAllowed(party.isExternalGuestAllowed())
                .guestFee(party.guestFee())
                .externalGuestFee(party.externalGuestFee())
                .information(party.information())
                .build();
    }

    private static String createMoods(List<String> moods) {
        return String.join(DELIMITER, moods);

    }

}
