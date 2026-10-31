import type { CognitiveLoadProfileDTO } from './employee';

export interface TeamScheduleAnalyticsDTO {
  totalAssignedHours: number;
  averageCognitiveScore: number;
  cognitiveScoreStdDev: number;
  scheduleFairnessScore: number;
  totalNightShifts: number;
  totalRestGapViolations: number;
  staffProfiles: CognitiveLoadProfileDTO[];
}
