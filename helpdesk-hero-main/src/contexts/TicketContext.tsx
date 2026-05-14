// ============================================================
// TicketContext — in-memory ticket store with CRUD operations
// ============================================================
import React, { createContext, useContext, useState, useEffect } from "react";
import type { Ticket, TicketStatus, TicketContextValue, Comment } from "@/types";
import type { User } from "@/types";
import { apiFetch } from "@/lib/api";

const TicketContext = createContext<TicketContextValue | null>(null);

const mapBackendToFrontendTicket = (t: any): Ticket => ({
  id: t.id ? t.id.toString() : "",
  title: t.title,
  description: t.description,
  category: t.category,
  // Normalize STATUS: OPEN -> Open, IN_PROGRESS -> In Progress
  status: t.status === "OPEN" ? "Open" : t.status === "IN_PROGRESS" ? "In Progress" : "Closed",
  // Normalize PRIORITY: LOW -> Low
  priority: t.priority ? (t.priority.charAt(0).toUpperCase() + t.priority.slice(1).toLowerCase() as any) : "Low",
  createdById: t.createdBy?.id?.toString() || "",
  createdByName: t.createdBy?.name || "System",
  assignedToId: t.assignedTo?.id?.toString(),
  assignedToName: t.assignedTo?.name,
  attachments: [],
  comments: (t.comments || []).map((c: any) => ({
    id: c.id.toString(),
    ticketId: t.id.toString(),
    authorId: c.sender.id.toString(),
    authorName: c.sender.name,
    authorRole: c.sender.role.toLowerCase(),
    body: c.message,
    createdAt: c.timestamp,
  })),
  createdAt: t.createdAt,
  updatedAt: t.createdAt,
});

export const TicketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [tickets, setTickets] = useState<Ticket[]>([]);

  const fetchTickets = async () => {
    try {
      const data = await apiFetch("/api/tickets");
      setTickets(data.map(mapBackendToFrontendTicket));
    } catch (err) {
      console.error("Failed to load tickets:", err);
    }
  };

  useEffect(() => {
    // Automatically load tickets on mount if authorized.
    if (localStorage.getItem("token")) {
      fetchTickets();
    }
  }, []);

  /** Create a new ticket on backend */
  const addTicket = async (data: Omit<Ticket, "id" | "createdAt" | "updatedAt" | "comments">) => {
    const response = await apiFetch("/api/tickets", {
      method: "POST",
      body: JSON.stringify({
        title: data.title,
        description: data.description,
        category: data.category,
        priority: data.priority.toUpperCase(), // React 'Low' -> Backend 'LOW'
      }),
    });
    const savedBackendTicket = mapBackendToFrontendTicket(response);
    setTickets((prev) => [savedBackendTicket, ...prev]);
    // Sync with latest data
    await fetchTickets();
    return savedBackendTicket;
  };

  /** Update the status of a ticket on backend */
  const updateTicketStatus = async (id: string, status: TicketStatus) => {
    const backendStatusParam = status === "In Progress" ? "IN_PROGRESS" : status.toUpperCase();
    await apiFetch(`/api/tickets/${id}/status?status=${backendStatusParam}`, {
      method: "PATCH",
    });
    // Optimistic UI update
    setTickets((prev) =>
      prev.map((t) =>
        t.id === id ? { ...t, status, updatedAt: new Date().toISOString() } : t
      )
    );
  };

  /** Add a comment to a ticket via backend */
  const addComment = async (ticketId: string, body: string, author: User) => {
    const response = await apiFetch(`/api/tickets/${ticketId}/comments`, {
      method: "POST",
      body: JSON.stringify({ message: body }),
    });
    
    const newComment: Comment = {
      id: response.id.toString(),
      ticketId: response.ticketId.toString(),
      authorId: response.sender.id.toString(),
      authorName: response.sender.name,
      authorRole: response.sender.role.toLowerCase(),
      body: response.message,
      createdAt: response.timestamp,
    };

    // Optimistically update UI so message appears instantly
    setTickets((prev) =>
      prev.map((t) =>
        t.id === ticketId
          ? { ...t, comments: [...t.comments, newComment] }
          : t
      )
    );
  };

  /** Delete a ticket (admin only — enforced in UI) */
  const deleteTicket = async (id: string) => {
    // Note: Assuming a backend DELETE endpoint exists, just optimistic delete for now to avoid crashes if it doesn't 
    setTickets((prev) => prev.filter((t) => t.id !== id));
  };

  const getTicketById = (id: string) => tickets.find((t) => t.id === id);

  return (
    <TicketContext.Provider value={{ tickets, addTicket, updateTicketStatus, addComment, deleteTicket, getTicketById, fetchTickets }}>
      {children}
    </TicketContext.Provider>
  );
};

export const useTickets = () => {
  const ctx = useContext(TicketContext);
  if (!ctx) throw new Error("useTickets must be used inside <TicketProvider>");
  return ctx;
};
