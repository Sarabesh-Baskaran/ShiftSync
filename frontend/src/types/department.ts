export type DepartmentType =
  | 'EMERGENCY'
  | 'INTENSIVE_CARE'
  | 'SURGERY'
  | 'GENERAL_WARD'
  | 'PEDIATRICS'
  | 'MATERNITY'
  | 'PSYCHIATRY'
  | 'OUTPATIENT';

export interface Department {
  id: number;
  name: string;
  departmentType: DepartmentType;
  baseStressLevel: number;
  requiredMinStaff: number;
  preferredStaffRatio: number;
}
