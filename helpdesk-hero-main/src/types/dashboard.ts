export interface DashboardSummary {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  closedTickets: number;
  resolvedToday: number;
  avgResolutionDays: number;
  pendingTickets: number;
  highPriority: number;
}

export interface RecentTicket extends Omit<Ticket, 'comments'> {
  // Backend TicketDTO without full comments for recent
}

export interface CategoryStat {
  category: string;
  count: number;
}

export interface TicketVolume {
  date: string;
  count: number;
}

