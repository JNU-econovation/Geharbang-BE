package guesthouse.guestHousePost.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Contact {

    private String instagramId;
    private String phoneNumber;
    private String webSite;
    private String reservationUrl;

    public Contact(String instagramId, String phoneNumber, String webSite) {
        this(instagramId, phoneNumber, webSite, "");
    }

    public Contact(String instagramId, String phoneNumber, String webSite, String reservationUrl) {
        this.instagramId = instagramId;
        this.phoneNumber = phoneNumber;
        this.webSite = webSite;
        this.reservationUrl = reservationUrl;
    }

    public String getReservationUrl() {
        return reservationUrl == null ? "" : reservationUrl;
    }
}
