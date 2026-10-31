import apiClient from './api';
import type { WellbeingCheckIn, WellbeingCheckInRequest } from '../types';

export const wellbeingService = {
  async recordCheckIn(request: WellbeingCheckInRequest): Promise<WellbeingCheckIn> {
    const response = await apiClient.post<WellbeingCheckIn>('/wellbeing/check-in', request);
    return response.data;
  },

  async getEmployeeCheckIns(employeeId: number): Promise<WellbeingCheckIn[]> {
    const response = await apiClient.get<WellbeingCheckIn[]>(`/wellbeing/employee/${employeeId}`);
    return response.data;
  },

  async getTodayCheckIns(): Promise<WellbeingCheckIn[]> {
    const response = await apiClient.get<WellbeingCheckIn[]>('/wellbeing/today');
    return response.data;
  },
};
