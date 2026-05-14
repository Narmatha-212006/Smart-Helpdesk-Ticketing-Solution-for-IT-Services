package com.helpdesk.backend.service;

import com.helpdesk.backend.dto.TicketDTO;
import com.helpdesk.backend.dto.UserDTO;
import com.helpdesk.backend.entity.Role;
import com.helpdesk.backend.entity.Ticket;
import com.helpdesk.backend.entity.TicketPriority;
import com.helpdesk.backend.entity.TicketStatus;
import com.helpdesk.backend.entity.User;
import com.helpdesk.backend.repository.CommentRepository;
import com.helpdesk.backend.repository.TicketRepository;
import com.helpdesk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public TicketDTO createTicket(TicketDTO request, String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority() != null ? request.getPriority() : TicketPriority.LOW)
                .status(TicketStatus.OPEN)
                .createdBy(creator)
                .build();

        // Auto Assign Logic
        autoAssignTicket(ticket);

        return mapToDTO(ticketRepository.save(ticket));
    }

    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TicketDTO> getTicketsByCreator(String username) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ticketRepository.findByCreatedBy(creator).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TicketDTO> getAssignedTickets(String username) {
        User agent = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ticketRepository.findByAssignedTo(agent).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TicketDTO getTicketById(Long id) {
        return ticketRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    public TicketDTO updateTicketStatus(Long id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        return mapToDTO(ticketRepository.save(ticket));
    }

    public TicketDTO assignTicket(Long id, Long agentId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        if (agent.getRole() != Role.AGENT && agent.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not an agent");
        }
        
        ticket.setAssignedTo(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return mapToDTO(ticketRepository.save(ticket));
    }

    private void autoAssignTicket(Ticket ticket) {
        // Find agents in the same department as the ticket category
        List<User> agents = userRepository.findByRole(Role.AGENT).stream()
                .filter(u -> ticket.getCategory().equalsIgnoreCase(u.getDepartment()))
                .collect(Collectors.toList());

        if (agents.isEmpty()) {
            // fallback to all agents if no department match
            agents = userRepository.findByRole(Role.AGENT);
        }

        if (agents.isEmpty()) return; // No agents available

        // Find least workload (fewest active tickets)
        List<Object[]> workloads = ticketRepository.findAgentWorkloads();
        
        User leastLoadedAgent = agents.get(0);
        long minLoad = Long.MAX_VALUE;

        for (User agent : agents) {
            long currentLoad = 0;
            for (Object[] w : workloads) {
                if (((User) w[0]).getId().equals(agent.getId())) {
                    currentLoad = (Long) w[1];
                    break;
                }
            }
            if (currentLoad < minLoad) {
                minLoad = currentLoad;
                leastLoadedAgent = agent;
            }
        }

        ticket.setAssignedTo(leastLoadedAgent);
    }

    private TicketDTO mapToDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .createdBy(mapUserDTO(ticket.getCreatedBy()))
                .assignedTo(ticket.getAssignedTo() != null ? mapUserDTO(ticket.getAssignedTo()) : null)
                .comments(commentRepository.findByTicketOrderByTimestampAsc(ticket).stream().map(c -> 
                        com.helpdesk.backend.dto.CommentDTO.builder()
                        .id(c.getId())
                        .ticketId(c.getTicket().getId())
                        .sender(mapUserDTO(c.getSender()))
                        .message(c.getMessage())
                        .timestamp(c.getTimestamp())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }

    private UserDTO mapUserDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .status(user.getStatus())
                .build();
    }
}
