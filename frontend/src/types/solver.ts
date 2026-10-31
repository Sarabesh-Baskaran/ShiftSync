export interface SolveRosterRequest {
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
  timeoutSeconds?: number;
  departmentId?: number;
  validateCapacityFirst?: boolean;
}

export interface ConstraintExplanationDTO {
  constraintName: string;
  constraintPackage: string;
  weightDescription: string;
  hardScoreImpact: number;
  mediumScoreImpact: number;
  softScoreImpact: number;
  matchCount: number;
  justificationSummary: string;
}

export interface SolverJobResponse {
  jobId: string;
  status: 'SOLVING' | 'SOLVED' | 'FAILED';
  feasible: boolean;
  hardScore: number;
  mediumScore: number;
  softScore: number;
  scoreSummary: string;
  totalShifts: number;
  assignedShifts: number;
  unassignedShifts: number;
  fairnessScore?: number;
  solvingDurationMs?: number;
  constraintBreakdown: ConstraintExplanationDTO[];
  capacityWarnings: string[];
}

export interface SkillSupplyDemandDTO {
  supplyCount: number;
  demandCount: number;
  deficit: number;
}

export interface CapacityAnalysisDTO {
  startDate: string;
  endDate: string;
  totalStaffCount: number;
  totalAvailableCapacityHours: number;
  totalShiftDemandHours: number;
  capacityFeasible: boolean;
  skillSupplyDemand: Record<string, SkillSupplyDemandDTO>;
  warnings: string[];
}
