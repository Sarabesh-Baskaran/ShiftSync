import apiClient from './api';
import type { CapacityAnalysisDTO, SolveRosterRequest, SolverJobResponse } from '../types';

export const solverService = {
  async solveRosterAsync(request: SolveRosterRequest): Promise<SolverJobResponse> {
    const response = await apiClient.post<SolverJobResponse>('/roster/solve', request);
    return response.data;
  },

  async solveRosterSync(request: SolveRosterRequest): Promise<SolverJobResponse> {
    const response = await apiClient.post<SolverJobResponse>('/roster/solve-sync', request);
    return response.data;
  },

  async getJobStatus(jobId: string): Promise<SolverJobResponse> {
    const response = await apiClient.get<SolverJobResponse>(`/roster/status/${jobId}`);
    return response.data;
  },

  async stopSolving(jobId: string): Promise<void> {
    await apiClient.post(`/roster/stop/${jobId}`);
  },

  async validateCapacity(
    startDate: string,
    endDate: string,
    departmentIds?: number[]
  ): Promise<CapacityAnalysisDTO> {
    const params: Record<string, string | string[]> = { startDate, endDate };
    if (departmentIds && departmentIds.length > 0) {
      params.departmentIds = departmentIds.map(String).join(',');
    }
    const response = await apiClient.get<CapacityAnalysisDTO>('/roster/validate-capacity', {
      params,
    });
    return response.data;
  },
};
