import type { Department } from './department';
import type { Employee, Skill } from './employee';

export type ShiftType = 'MORNING' | 'EVENING' | 'NIGHT';

export type ShiftStatus = 'UNASSIGNED' | 'ASSIGNED' | 'COMPLETED' | 'CANCELLED';

export interface Shift {
  id: number;
  department: Department;
  shiftType: ShiftType;
  shiftDate: string; // YYYY-MM-DD
  startTime: string; // HH:mm:ss
  endTime: string;   // HH:mm:ss
  requiredSkill: Skill;
  assignedEmployee: Employee | null;
  status: ShiftStatus;
  stressWeight: number;
}
