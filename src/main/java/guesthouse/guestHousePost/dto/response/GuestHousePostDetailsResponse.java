package guesthouse.guestHousePost.dto.response;

import guesthouse.guestHousePost.domain.model.Amenity;
import guesthouse.guestHousePost.domain.vo.*;
import guesthouse.guestHousePost.dto.GuestHousePostDetailsDTO;
import guesthouse.guestHousePost.dto.PartyWithImageUrlDTO;
import guesthouse.guestHousePost.dto.RoomWithImageUrlDTO;
import guesthouse.staffrecruitment.domain.vo.Region;
import lombok.Builder;
import org.locationtech.jts.geom.Point;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Builder
public record GuestHousePostDetailsResponse (
        String guestHouseName,
        Region region,
        Location location,
        List<Room> rooms,
        String introduction,
        List<String> amenities,
        Set<Mood> moods,
        List<Party> parties,
        Contact contact,
        String ownerMessage
){
    private static final String SEPARATOR = "\\|:\\|";

    public static GuestHousePostDetailsResponse from(GuestHousePostDetailsDTO details) {

        return GuestHousePostDetailsResponse.builder()
                .guestHouseName(details.guestHouseName())
                .region(details.region())
                .location(Location.from(details.lotNumberAddress(), details.roadNameAddress(), details.coordinates()))
                .rooms(Room.from(details.rooms()))
                .introduction(details.introduction())
                .amenities(getAmenityValues(details.amenities()))
                .moods(details.moods())
                .parties(Party.from(details.parties()))
                .contact(details.contact())
                .ownerMessage(details.ownerMessage())
                .build();
    }


    private record Location (
            String lotNumberAddress,
            String roadNameAddress,
            double[] coordinates
    ){
        public static Location from(String lotNumberAddress, String roadNameAddress, Point coordinates) {
            return new Location(
                    lotNumberAddress,
                    roadNameAddress,
                    new double[]{coordinates.getY(), coordinates.getX()}
            );
        }
    }

    private record Room (
            String name,
            RoomType type,
            RoomHeadCount headCountType,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            int pricePerNight,
            List<String> imageUrls
    ){
        public static List<Room> from(List<RoomWithImageUrlDTO> rooms) {
            return rooms
                    .stream()
                    .map(dto ->
                            new Room(
                                    dto.name(), dto.type(), dto.headCount(),
                                    dto.checkInTime(), dto.checkOutTime(),
                                    dto.pricePerNight(), dto.imageUrls()
                            )
                    )
                    .toList();
        }
    }

    private record Party (
            PartyType type,
            String otherPartyType,
            LocalTime startTime,
            LocalTime endTime,
            Set<DayOfWeek> weeklyDays,
            String place,
            List<String> moods,
            Boolean isExternalGuestAllowed,
            Long guestFee,
            Long externalGuestFee,
            List<String> imageUrls
    ){
        public static List<Party> from(List<PartyWithImageUrlDTO> parties) {
            return parties
                    .stream()
                    .map(dto ->
                            new Party(
                                    dto.type(), dto.otherPartyType(), dto.startTime(),
                                    dto.endTime(), dto.weeklyDays(), dto.place(),
                                    splitBySeparator(dto.moods()), dto.isExternalGuestAllowed(), dto.guestFee(),
                                    dto.externalGuestFee(), dto.imageUrls()
                            )
                    )
                    .toList();
        }
    }

    private static List<String> getAmenityValues(List<Amenity> amenities) {
        return amenities
                .stream()
                .map(Amenity::getValue)
                .toList();
    }


    private static List<String> splitBySeparator(String str) {
        List<String> result = new ArrayList<>();

        if (str != null && !str.isBlank())
            result = Arrays.asList(str.split(SEPARATOR));

        return result;
    }
}
