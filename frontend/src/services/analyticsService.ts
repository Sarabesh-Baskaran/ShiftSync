import apiClient from './api';
import type { CognitiveLoadProfileDTO, TeamScheduleAnalyticsDTO } from '../types';

export const analyticsService = {
  async getStaffAnalytics(
    employeeId: number,
    startDate?: string,
    endDate?: string
  ): Promise<CognitiveLoadProfileDTO> {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await apiClient.get<CognitiveLoadProfileDTO>(`/analytics/staff/${employeeId}`, {
      params,
    });
    return response.data;
  },

  async getTeamAnalytics(
    startDate?: string,
    endDate?: string,
    departmentId?: number
  ): Promise<TeamScheduleAnalyticsDTO> {
    const params: Record<string, string | number> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    if (departmentId) params.departmentId = departmentId;

    const response = await apiClient.get<TeamScheduleAnalyticsDTO>('/analytics/team', {
      params,
    });
    return response.data;
  },
};
