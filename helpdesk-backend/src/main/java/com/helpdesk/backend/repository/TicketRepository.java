package com.helpdesk.backend.repository;

import com.helpdesk.backend.entity.Ticket;
import com.helpdesk.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedBy(User user);
    List<Ticket> findByAssignedTo(User user);
    List<Ticket> findByCategory(String category);

    @Query("SELECT t.assignedTo, COUNT(t) FROM Ticket t WHERE t.assignedTo IS NOT NULL AND t.status NOT IN ('RESOLVED', 'CLOSED') GROUP BY t.assignedTo ORDER BY COUNT(t) ASC")
    List<Object[]> findAgentWorkloads();
}
