package com.helpdesk.backend.service;

import com.helpdesk.backend.dto.CommentDTO;
import com.helpdesk.backend.dto.UserDTO;
import com.helpdesk.backend.entity.Comment;
import com.helpdesk.backend.entity.Ticket;
import com.helpdesk.backend.entity.User;
import com.helpdesk.backend.repository.CommentRepository;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentDTO addComment(Long ticketId, CommentDTO request, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        User sender = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .sender(sender)
                .message(request.getMessage())
                .build();

        return mapToDTO(commentRepository.save(comment));
    }

    public List<CommentDTO> getCommentsByTicketId(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        return commentRepository.findByTicketOrderByTimestampAsc(ticket)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO mapToDTO(Comment comment) {
        UserDTO senderDTO = UserDTO.builder()
                .id(comment.getSender().getId())
                .name(comment.getSender().getName())
                .email(comment.getSender().getEmail())
                .role(comment.getSender().getRole())
                .build();

        return CommentDTO.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .sender(senderDTO)
                .message(comment.getMessage())
                .timestamp(comment.getTimestamp())
                .build();
    }
}
