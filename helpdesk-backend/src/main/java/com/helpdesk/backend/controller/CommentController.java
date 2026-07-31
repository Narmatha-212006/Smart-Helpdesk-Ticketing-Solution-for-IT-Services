package com.helpdesk.backend.controller;

import com.helpdesk.backend.dto.CommentDTO;
import com.helpdesk.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long ticketId,
            @RequestBody CommentDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.addComment(ticketId, request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsByTicketId(ticketId));
    }
}
