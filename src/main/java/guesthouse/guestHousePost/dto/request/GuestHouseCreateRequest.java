package guesthouse.guestHousePost.dto.request;

import guesthouse.guestHousePost.domain.vo.*;
import guesthouse.staffrecruitment.domain.vo.Region;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;


public record GuestHouseCreateRequest (

        @NotBlank
        @Length(min = 2, max = 30)
        String guestHouseName,

        @NotNull
        Region region,

        @Valid
        @NotNull
        Location location,

        @NotEmpty
        @Size(min = 1, max = 10)
        List<String> imageUrls,

        @NotBlank
        @Length(min = 10, max = 500)
        String introduction,

        @NotEmpty
        @Size(min = 1, max = 10)
        List<@Length(min = 1, max = 20) String> amenities,

        @NotEmpty
        @Size(min = 1, max = 2)
        Set<Mood> moods,

        @Valid
        @Size(min = 0, max = 10)
        List<Party> parties,

        @Valid
        @Size(min = 1, max =  10)
        List<Room> rooms,

        @Valid
        Contact contact,

        @Length(max = 50)
        String ownerMessage

) {

    public record Location(

            @NotBlank
            String lotNumberAddress,

            @NotBlank
            String roadNameAddress,

            @Size(min = 2, max = 2)
            List<Double> coordinates
    ) {
    }

    public record Party (

            @NotNull
            PartyType type,

            @Length (min = 1, max = 20)
            String otherPartyType,

            @NotEmpty
            @Size(min = 1, max = 10)
            List<String> imageUrls,

            @NotNull
            LocalTime startTime,

            @NotNull
            LocalTime endTime,

            @NotEmpty
            Set<DayOfWeek> weeklyDays,

            @NotBlank
            @Length(min = 1, max = 20)
            String place,

            @NotEmpty
            List<@Length( min = 1, max = 20) String> moods,

            @NotNull
            Boolean isExternalGuestAllowed,

            @NotNull
            @PositiveOrZero
            Long guestFee,

            @PositiveOrZero
            Long externalGuestFee,

            @NotBlank
            @Length(min = 10, max = 500)
            String information
    ) {
    }

    public record Room(

            @NotBlank
            @Length (min = 1, max = 20)
            String name,

            @NotNull
            RoomType type,

            @NotNull
            RoomHeadCount headCountType,

            @NotNull
            LocalTime checkInTime,

            @NotNull
            LocalTime checkOutTime,

            @NotNull
            @PositiveOrZero
            Integer pricePerNight,

            @NotEmpty
            @Size(min = 1, max = 10)
            List<String> imageUrls
    ) {
    }

    public record Contact(

            @Length(min = 11, max = 13)
            String phoneNumber,

            @Length(min = 2, max = 30)
            String instagramId,

            @Length(max = 100)
            String webSite
    ) {
    }
}