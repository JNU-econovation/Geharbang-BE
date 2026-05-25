package guesthouse.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpoPushTicket(
        String status,
        String id,
        String message,
        Map<String, String> details
) {
    public boolean isDeviceNotRegistered() {
        return "error".equals(status)
                && details != null
                && "DeviceNotRegistered".equals(details.get("error"));
    }
}
