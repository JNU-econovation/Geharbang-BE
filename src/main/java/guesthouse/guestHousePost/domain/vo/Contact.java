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

    public Contact(String instagramId, String phoneNumber, String webSite) {
        this.instagramId = instagramId;
        this.phoneNumber = phoneNumber;
        this.webSite = webSite;
    }
}
