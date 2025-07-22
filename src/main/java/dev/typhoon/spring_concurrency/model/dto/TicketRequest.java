package dev.typhoon.spring_concurrency.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketRequest {

    private String ticketName;
    private Long quantity;
    private boolean version;
}
