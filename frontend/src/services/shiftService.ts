import apiClient from './api';
import type { Shift } from '../types';

export const shiftService = {
  async getShifts(startDate: string, endDate: string, departmentId?: number): Promise<Shift[]> {
    const params: Record<string, string | number> = { startDate, endDate };
    if (departmentId) {
      params.departmentId = departmentId;
    }
    const response = await apiClient.get<Shift[]>('/shifts', { params });
    return response.data;
  },

  async getShiftById(id: number): Promise<Shift> {
    const response = await apiClient.get<Shift>(`/shifts/${id}`);
    return response.data;
  },

  async assignShift(shiftId: number, employeeId: number): Promise<Shift> {
    const response = await apiClient.put<Shift>(`/shifts/${shiftId}/assign`, null, {
      params: { employeeId },
    });
    return response.data;
  },

  async unassignShift(shiftId: number): Promise<Shift> {
    const response = await apiClient.put<Shift>(`/shifts/${shiftId}/unassign`);
    return response.data;
  },
};
