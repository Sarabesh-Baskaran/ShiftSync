import apiClient from './api';

export interface DatabaseStatus {
  departments: number;
  staff: number;
  shifts: number;
  checkIns: number;
}

export const demoService = {
  async getStatus(): Promise<DatabaseStatus> {
    const response = await apiClient.get<DatabaseStatus>('/demo/status');
    return response.data;
  },

  async seedData(forceReset: boolean = true): Promise<Record<string, unknown>> {
    const response = await apiClient.post<Record<string, unknown>>('/demo/seed', null, {
      params: { forceReset },
    });
    return response.data;
  },

  async resetData(): Promise<{ message: string }> {
    const response = await apiClient.delete<{ message: string }>('/demo/reset');
    return response.data;
  },
};
