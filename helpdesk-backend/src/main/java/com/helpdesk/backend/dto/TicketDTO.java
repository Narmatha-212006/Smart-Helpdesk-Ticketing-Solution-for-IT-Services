package com.helpdesk.backend.dto;

import com.helpdesk.backend.entity.TicketPriority;
import com.helpdesk.backend.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private TicketPriority priority;
    private TicketStatus status;
    private UserDTO createdBy;
    private UserDTO assignedTo;
    private List<CommentDTO> comments;
    private LocalDateTime createdAt;
}
