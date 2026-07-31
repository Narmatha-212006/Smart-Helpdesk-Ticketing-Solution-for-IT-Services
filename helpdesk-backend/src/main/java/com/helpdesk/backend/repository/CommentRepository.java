package com.helpdesk.backend.repository;

import com.helpdesk.backend.entity.Comment;
import com.helpdesk.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicketOrderByTimestampAsc(Ticket ticket);
}
