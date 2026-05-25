package guesthouse.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpoPushResponse(List<ExpoPushTicket> data) {}
