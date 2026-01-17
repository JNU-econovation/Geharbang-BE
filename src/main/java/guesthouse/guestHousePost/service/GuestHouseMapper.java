package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.Contact;
import guesthouse.guestHousePost.domain.vo.Status;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class GuestHouseMapper {

    public static GuestHousePost toPost(GuestHouseCreateRequest request, Long userId) {
        GuestHouseCreateRequest.Location location = request.location();
        GuestHouseCreateRequest.Contact contact = request.contact();

        return GuestHousePost.builder()
                .ownerId(userId)
                .guestHouseName(request.guestHouseName())
                .region(request.region())
                .lotNumberAddress(location.lotNumberAddress())
                .roadNameAddress(location.roadNameAddress())
                .coordinates(createPoint(location.coordinates()))
                .introduction(request.introduction())
                .moods(request.moods())
                .contact(createContact(contact))
                .ownerMessage(request.ownerMessage())
                .status(Status.ACTIVE)
                .build();
    }

    private static Point createPoint(List<Double> coordinates) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(coordinates.getFirst(), coordinates.getLast()));
    }

    private static Contact createContact(GuestHouseCreateRequest.Contact contact) {
        return new Contact(
                Objects.requireNonNullElse(contact.instagramId(), ""),
                Objects.requireNonNullElse(contact.phoneNumber(), ""),
                Objects.requireNonNullElse(contact.webSite(), "")
        );
    }
}
