package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.Contact;
import guesthouse.staffrecruitment.domain.model.Feature;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.Status;
import guesthouse.staffrecruitment.dto.request.StaffRecruitmentCreateRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class StaffRecruitmentMapper {

    public static final String DELIMITER = "|:|";

    private StaffRecruitmentMapper() {

    }

    public static StaffRecruitment from(StaffRecruitmentCreateRequest request, Long ownerId) {
        StaffRecruitmentCreateRequest.Contact contact = request.contact();
        StaffRecruitmentCreateRequest.Introduction introduction = request.introduction();
        StaffRecruitmentCreateRequest.Feature feature = request.feature();
        StaffRecruitmentCreateRequest.Location location = request.location();
        StaffRecruitmentCreateRequest.WorkingInformation workingInformation = request.workingInformation();

        List<Double> coordinates = location.coordinates();

        return StaffRecruitment.builder()
                .ownerId(ownerId)
                .title(request.title())
                .guestHouseName(request.guestHouseName())
                .region(request.region())
                .lotNumberAddress(location.lotNumberAddress())
                .roadNameAddress(location.roadNameAddress())
                .coordinates(createPoint(coordinates))
                .startDate(workingInformation.startDate())
                .workingPeriod(workingInformation.workingPeriod())
                .contact(createContact(contact))
                .feature(createFeature(feature))
                .content(introduction.content())
                .ownerMessage(request.ownerMessage())
                .status(Status.ACTIVE)
                .build();
    }

    private static Point createPoint(List<Double> coordinates) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(coordinates.getFirst(), coordinates.getLast()));
    }

    private static Contact createContact(StaffRecruitmentCreateRequest.Contact contact) {
        return new Contact(
                Objects.requireNonNullElse(contact.instagramId(), ""),
                Objects.requireNonNullElse(contact.phoneNumber(), ""),
                Objects.requireNonNullElse(contact.webSite(), "")
        );
    }

    private static Feature createFeature(StaffRecruitmentCreateRequest.Feature feature) {
        return new Feature(
                feature.gender(),
                Optional.ofNullable(feature.advantages())
                        .map(list -> String.join(DELIMITER, list))
                        .orElse(""),
                Optional.ofNullable(feature.employeeBenefits())
                        .map(list -> String.join(DELIMITER, list))
                        .orElse("")
        );
    }

}
