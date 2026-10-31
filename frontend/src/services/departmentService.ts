import apiClient from './api';
import type { Department } from '../types';

export const departmentService = {
  async getDepartments(): Promise<Department[]> {
    const response = await apiClient.get<Department[]>('/departments');
    return response.data;
  },

  async getDepartmentById(id: number): Promise<Department> {
    const response = await apiClient.get<Department>(`/departments/${id}`);
    return response.data;
  },
};
