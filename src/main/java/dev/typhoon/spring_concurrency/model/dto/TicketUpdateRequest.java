package dev.typhoon.spring_concurrency.model.dto;

import lombok.Data;

@Data
public class TicketUpdateRequest {
    
    private String ticketName;
    private Long quantity;
    private boolean isVersion;
}
