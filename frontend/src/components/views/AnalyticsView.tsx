import React, { useEffect, useState } from 'react';
import { BarChart3, AlertTriangle } from 'lucide-react';
import type { TeamScheduleAnalyticsDTO } from '../../types';
import { analyticsService } from '../../services/analyticsService';
import { Badge } from '../common/Badge';

export const AnalyticsView: React.FC = () => {
  const [analytics, setAnalytics] = useState<TeamScheduleAnalyticsDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchAnalytics = async () => {
      setLoading(true);
      try {
        const today = new Date().toISOString().split('T')[0];
        const nextWeek = new Date(Date.now() + 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
        const data = await analyticsService.getTeamAnalytics(today, nextWeek);
        setAnalytics(data);
      } catch (err: unknown) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError('Failed to fetch team analytics.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, []);

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-indigo-800/40 bg-gradient-to-r from-indigo-950/40 via-slate-900/60 to-slate-950 p-6">
        <div className="space-y-1">
          <div className="flex items-center space-x-2">
            <BarChart3 className="h-6 w-6 text-indigo-400" />
            <h2 className="text-xl font-bold text-white">Cognitive Load & Fairness Analytics</h2>
            <span className="rounded-full bg-indigo-500/10 px-2.5 py-0.5 text-xs font-semibold text-indigo-400 border border-indigo-500/20">
              Algorithmic Telemetry
            </span>
          </div>
          <p className="text-sm text-slate-400 max-w-2xl">
            Real-time evaluation of schedule standard deviation, circadian night shifts distribution,
            and rest-gap violation monitoring across all healthcare teams.
          </p>
        </div>
      </div>

      {loading && (
        <div className="text-center py-12 text-slate-400 text-sm animate-pulse">
          Calculating hospital cognitive strain & fairness index...
        </div>
      )}

      {error && (
        <div className="p-4 rounded-xl bg-amber-950/60 border border-amber-800/60 text-amber-300 text-sm flex items-center space-x-2">
          <AlertTriangle className="h-5 w-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {analytics && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-5 rounded-2xl bg-slate-900/40 border border-slate-800 space-y-1">
              <div className="text-xs text-slate-400">Team Fairness Score</div>
              <div className="text-3xl font-bold font-mono text-purple-400">
                {analytics.scheduleFairnessScore.toFixed(1)}%
              </div>
              <div className="text-[11px] text-slate-500">
                Inverse of cognitive variance across clinicians
              </div>
            </div>

            <div className="p-5 rounded-2xl bg-slate-900/40 border border-slate-800 space-y-1">
              <div className="text-xs text-slate-400">Rest Gap Violations</div>
              <div className="text-3xl font-bold font-mono text-emerald-400">
                {analytics.totalRestGapViolations}
              </div>
              <div className="text-[11px] text-slate-500">
                Zero "clopening" violations enforced by Timefold
              </div>
            </div>

            <div className="p-5 rounded-2xl bg-slate-900/40 border border-slate-800 space-y-1">
              <div className="text-xs text-slate-400">Assigned Clinical Hours</div>
              <div className="text-3xl font-bold font-mono text-cyan-400">
                {analytics.totalAssignedHours}h
              </div>
              <div className="text-[11px] text-slate-500">
                Across {analytics.totalNightShifts} night shifts
              </div>
            </div>
          </div>

          {/* Clinician Profiles Table */}
          <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-5 space-y-4">
            <h3 className="font-semibold text-white text-sm">Individual Clinician Risk Profiles</h3>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400">
                    <th className="pb-3 font-semibold">Clinician</th>
                    <th className="pb-3 font-semibold">Department</th>
                    <th className="pb-3 font-semibold">Shifts</th>
                    <th className="pb-3 font-semibold">Hours</th>
                    <th className="pb-3 font-semibold">Night Shifts</th>
                    <th className="pb-3 font-semibold">Cognitive Score</th>
                    <th className="pb-3 font-semibold">Burnout Risk</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {analytics.staffProfiles.map((p) => (
                    <tr key={p.employeeId} className="hover:bg-slate-800/30 transition">
                      <td className="py-3 font-medium text-slate-200">{p.employeeName}</td>
                      <td className="py-3 text-slate-400">{p.primaryDepartment}</td>
                      <td className="py-3 font-mono text-slate-300">{p.totalShifts}</td>
                      <td className="py-3 font-mono text-slate-300">{p.totalHours}h</td>
                      <td className="py-3 font-mono text-slate-300">{p.nightShiftsCount}</td>
                      <td className="py-3 font-mono font-semibold text-teal-400">
                        {p.cumulativeCognitiveScore.toFixed(1)}
                      </td>
                      <td className="py-3">
                        <Badge variant="risk" riskLevel={p.burnoutRisk}>
                          {p.burnoutRisk}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
