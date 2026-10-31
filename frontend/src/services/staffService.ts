import apiClient from './api';
import type { Employee } from '../types';

export const staffService = {
  async getAllStaff(departmentId?: number): Promise<Employee[]> {
    const params = departmentId ? { departmentId } : {};
    const response = await apiClient.get<Employee[]>('/staff', { params });
    return response.data;
  },

  async getStaffById(id: number): Promise<Employee> {
    const response = await apiClient.get<Employee>(`/staff/${id}`);
    return response.data;
  },
};
