package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.Amenity;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class AmenityMapper {

    public static List<Amenity> toAmenities(List<String> amenities, Long guestHousePostId) {
        return amenities
                .stream()
                .map(value -> createdAmenities(value, guestHousePostId))
                .toList();
    }

    private static Amenity createdAmenities(String value, Long guestHousePostId) {
        return Amenity.builder()
                .value(value)
                .guestHousePostId(guestHousePostId)
                .build();
    }


}
