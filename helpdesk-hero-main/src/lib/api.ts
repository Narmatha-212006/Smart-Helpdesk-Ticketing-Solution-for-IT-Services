import type { DashboardSummary, Ticket, User } from "@/types";
export const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const getAuthToken = () => localStorage.getItem('token');

/**
 * A generalized custom fetch wrapper that automatically injects the JWT token
 * and prefixes the correct environment-based Backend API URL.
 */
export async function apiFetch(endpoint: string, options: RequestInit = {}) {
  const token = getAuthToken();
  const headers = new Headers(options.headers || {});
  
  headers.set('Content-Type', 'application/json');
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || 'API Request Failed');
  }

  return response.json();
}

/**
 * Register a new user account without auto-logging in.
 */
export async function registerAccount(fullName: string, email: string, password: string, role: string) {
  return apiFetch('/api/auth/register', {
    method: 'POST',
    // We map fullName to the 'name' property expected by the Java backend DTO
    body: JSON.stringify({ name: fullName, email, password, role }),
  });
}

/**
 * Fetch user-specific dashboard statistics.
 */
export async function getDashboardStats(): Promise<DashboardSummary> {
  return apiFetch('/api/dashboard/user-stats');
}

/**
 * Fetch recent tickets for current user.
 */
export async function getRecentTickets(limit: number = 5): Promise<Ticket[]> {
  return apiFetch(`/api/tickets/recent?limit=${limit}`);
}

/**
 * Fetch current user's profile.
 */
export async function getCurrentUser(): Promise<User> {
  return apiFetch('/api/users/me');
}

/**
 * Fetch current user's tickets.
 */
export async function getMyTickets(): Promise<Ticket[]> {
  return apiFetch('/api/tickets/my');
}

/**
 * Fetch dashboard category statistics.
 */
export async function getCategoryStats(): Promise<{ category: string; count: number }[]> {
  return apiFetch('/api/dashboard/categories');
}

/**
 * Fetch dashboard ticket volume data (last 14 days).
 */
export async function getTicketVolume(): Promise<{ date: string; count: number }[]> {
  return apiFetch('/api/dashboard/volume');
}

