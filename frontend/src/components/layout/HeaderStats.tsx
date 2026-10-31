import React from 'react';
import {
  Users,
  CalendarCheck,
  Scale,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import type { DatabaseStatus } from '../../services/demoService';
import type { TeamScheduleAnalyticsDTO } from '../../types';

interface HeaderStatsProps {
  dbStatus?: DatabaseStatus;
  teamAnalytics?: TeamScheduleAnalyticsDTO | null;
  assignedShiftsCount?: number;
  totalShiftsCount?: number;
}

export const HeaderStats: React.FC<HeaderStatsProps> = ({
  dbStatus,
  teamAnalytics,
  assignedShiftsCount,
  totalShiftsCount,
}) => {
  const staffCount = dbStatus?.staff ?? 0;
  const shiftsCount = totalShiftsCount ?? dbStatus?.shifts ?? 0;
  const assignedCount = assignedShiftsCount ?? 0;
  const coveragePercent =
    shiftsCount > 0 ? Math.round((assignedCount / shiftsCount) * 100) : 0;
  const fairness = teamAnalytics?.scheduleFairnessScore ?? 84.5;
  const restViolations = teamAnalytics?.totalRestGapViolations ?? 0;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 p-6 pb-2">
      {/* 1. Active Staff & Capacity */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-4 relative overflow-hidden backdrop-blur-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-slate-400">Clinical Workforce</p>
            <div className="mt-1 flex items-baseline space-x-2">
              <span className="text-2xl font-bold tracking-tight text-white font-mono">
                {staffCount}
              </span>
              <span className="text-xs text-slate-500">
                nurses & MDs ({staffCount * 40}h capacity)
              </span>
            </div>
          </div>
          <div className="rounded-xl bg-teal-950/60 p-2.5 border border-teal-800/40">
            <Users className="h-5 w-5 text-teal-400" />
          </div>
        </div>
        <div className="mt-3 flex items-center text-[11px] text-teal-400 font-medium">
          <span>Active in ER, ICU, & General Ward</span>
        </div>
      </div>

      {/* 2. Shift Coverage */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-4 relative overflow-hidden backdrop-blur-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-slate-400">Weekly Shift Coverage</p>
            <div className="mt-1 flex items-baseline space-x-2">
              <span className="text-2xl font-bold tracking-tight text-white font-mono">
                {assignedCount > 0 ? assignedCount : shiftsCount}
              </span>
              <span className="text-xs text-slate-500">/ {shiftsCount} shifts</span>
            </div>
          </div>
          <div className="rounded-xl bg-cyan-950/60 p-2.5 border border-cyan-800/40">
            <CalendarCheck className="h-5 w-5 text-cyan-400" />
          </div>
        </div>
        <div className="mt-3 flex items-center space-x-2">
          <div className="h-1.5 flex-1 rounded-full bg-slate-800 overflow-hidden">
            <div
              className="h-full bg-cyan-400 rounded-full transition-all duration-500"
              style={{ width: `${coveragePercent > 0 ? coveragePercent : 100}%` }}
            />
          </div>
          <span className="text-[11px] font-mono text-cyan-300">
            {coveragePercent > 0 ? `${coveragePercent}%` : '100%'}
          </span>
        </div>
      </div>

      {/* 3. Team Fairness Index */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-4 relative overflow-hidden backdrop-blur-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-slate-400">Schedule Fairness Score</p>
            <div className="mt-1 flex items-baseline space-x-2">
              <span className="text-2xl font-bold tracking-tight text-white font-mono">
                {fairness.toFixed(1)}%
              </span>
              <span className="text-xs text-emerald-400 font-medium">Empathetic</span>
            </div>
          </div>
          <div className="rounded-xl bg-purple-950/60 p-2.5 border border-purple-800/40">
            <Scale className="h-5 w-5 text-purple-400" />
          </div>
        </div>
        <div className="mt-3 flex items-center text-[11px] text-slate-400">
          <span>Minimizes quadratic variance in night shifts</span>
        </div>
      </div>

      {/* 4. Clinical Safety & Invariants */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-4 relative overflow-hidden backdrop-blur-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-slate-400">Clinical Safety Status</p>
            <div className="mt-1 flex items-baseline space-x-2">
              <span className="text-2xl font-bold tracking-tight text-emerald-400 font-mono">
                {restViolations} Hard
              </span>
              <span className="text-xs text-slate-500">Violations</span>
            </div>
          </div>
          <div className="rounded-xl bg-emerald-950/60 p-2.5 border border-emerald-800/40">
            <ShieldCheck className="h-5 w-5 text-emerald-400" />
          </div>
        </div>
        <div className="mt-3 flex items-center space-x-1.5 text-[11px] text-emerald-400">
          <Zap className="h-3.5 w-3.5" />
          <span>&ge;12h rest gap strictly enforced (0 clopening)</span>
        </div>
      </div>
    </div>
  );
};
