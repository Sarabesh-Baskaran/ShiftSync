import type { Employee } from './employee';
import type { Shift } from './shift';

export interface WellbeingCheckIn {
  id: number;
  employee: Employee;
  shift?: Shift;
  selfReportedFatigue: number;
  perceivedStress: number;
  notes?: string;
  checkInTimestamp: string;
}

export interface WellbeingCheckInRequest {
  employeeId: number;
  shiftId?: number;
  selfReportedFatigue: number;
  perceivedStress: number;
  notes?: string;
}
