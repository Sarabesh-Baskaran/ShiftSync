import type { Department } from './department';

export type Skill =
  | 'TRAUMA_CARE'
  | 'ICU_CERTIFIED'
  | 'PEDIATRICS'
  | 'SURGICAL_ASSIST'
  | 'TRIAGE'
  | 'BLS_ACLS'
  | 'MED_SURG'
  | 'PHLEBOTOMY';

export interface Employee {
  id: number;
  name: string;
  email: string;
  primaryDepartment: Department;
  skills: Skill[];
  maxWeeklyHours: number;
  baselineFatigue: number;
  active: boolean;
}

export type BurnoutRiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';

export interface RestGapViolation {
  priorShiftId: number;
  priorShiftDate: string;
  priorShiftType: string;
  subsequentShiftId: number;
  subsequentShiftDate: string;
  subsequentShiftType: string;
  gapHours: number;
  minimumRequiredHours: number;
}

export interface CognitiveLoadProfileDTO {
  employeeId: number;
  employeeName: string;
  primaryDepartment: string;
  totalShifts: number;
  totalHours: number;
  nightShiftsCount: number;
  restGapViolationCount: number;
  cumulativeCognitiveScore: number;
  burnoutRisk: BurnoutRiskLevel;
  burnoutRiskColor: string;
  restGapViolations: RestGapViolation[];
  recommendations: string[];
}
