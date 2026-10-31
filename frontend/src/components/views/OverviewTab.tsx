import React from 'react';
import type { Department, Employee, Shift } from '../../types';
import { Badge } from '../common/Badge';
import { Building2, Stethoscope, ArrowRight } from 'lucide-react';
import type { ActiveTab } from '../layout/Sidebar';

interface OverviewTabProps {
  departments: Department[];
  staff: Employee[];
  shifts: Shift[];
  onNavigate: (tab: ActiveTab) => void;
  isLoading: boolean;
}

export const OverviewTab: React.FC<OverviewTabProps> = ({
  departments,
  staff,
  shifts,
  onNavigate,
  isLoading,
}) => {
  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="rounded-2xl border border-teal-800/40 bg-gradient-to-r from-teal-950/40 via-slate-900/60 to-slate-950 p-6">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="space-y-1">
            <h2 className="text-xl font-bold text-white flex items-center gap-2">
              <span>ShiftSync Operations Center</span>
              <span className="rounded-full bg-teal-500/10 px-2.5 py-0.5 text-xs font-semibold text-teal-400 border border-teal-500/20">
                Phase 3 Active
              </span>
            </h2>
            <p className="text-sm text-slate-400 max-w-2xl">
              Welcome to the ShiftSync interactive clinical cockpit. Powered by the Timefold constraint
              solver and our cognitive load analytics engine, ensuring zero clopening violations, fair
              circadian strain distribution, and protected healthcare workforce wellbeing.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => onNavigate('solver')}
              className="flex items-center space-x-2 rounded-xl bg-teal-600 hover:bg-teal-500 text-white px-4 py-2.5 text-sm font-semibold shadow-lg shadow-teal-900/30 transition cursor-pointer"
            >
              <span>Launch AI Solver ({shifts.length} shifts)</span>
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Department Breakdown */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Building2 className="h-5 w-5 text-teal-400" />
              <h3 className="font-semibold text-white text-sm">Hospital Departments</h3>
            </div>
            <span className="text-xs text-slate-500 font-mono">{departments.length} units</span>
          </div>

          <div className="space-y-2.5">
            {departments.map((dept) => (
              <div
                key={dept.id}
                className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800/70 hover:border-slate-700 transition"
              >
                <div>
                  <div className="text-sm font-medium text-slate-200">{dept.name}</div>
                  <div className="text-xs text-slate-500">
                    Min Staff: {dept.requiredMinStaff} • Ratio: 1:{dept.preferredStaffRatio}
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-mono font-semibold px-2 py-0.5 rounded bg-slate-800 text-teal-400 border border-slate-700">
                    Stress {dept.baseStressLevel}x
                  </span>
                </div>
              </div>
            ))}
            {departments.length === 0 && !isLoading && (
              <div className="text-xs text-slate-500 text-center py-4">
                No departments found. Click "Seed Demo Data" above to initialize.
              </div>
            )}
          </div>
        </div>

        {/* Clinical Staff Roster Highlights */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-5 space-y-4 lg:col-span-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Stethoscope className="h-5 w-5 text-cyan-400" />
              <h3 className="font-semibold text-white text-sm">Active Clinical Workforce</h3>
            </div>
            <button
              onClick={() => onNavigate('staff')}
              className="text-xs text-cyan-400 hover:text-cyan-300 font-medium flex items-center gap-1 cursor-pointer"
            >
              <span>View all {staff.length} staff</span>
              <ArrowRight className="h-3 w-3" />
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {staff.slice(0, 6).map((member) => (
              <div
                key={member.id}
                className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/70 hover:border-slate-700 transition space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div className="font-medium text-sm text-slate-200">{member.name}</div>
                  <span className="text-[11px] text-slate-500 font-mono">
                    {member.maxWeeklyHours}h/wk
                  </span>
                </div>
                <div className="text-xs text-slate-400">
                  {member.primaryDepartment?.name || 'Float Pool'}
                </div>
                <div className="flex flex-wrap gap-1 pt-1">
                  {member.skills?.map((skill) => (
                    <Badge key={skill} variant="skill">
                      {skill.replace('_', ' ')}
                    </Badge>
                  ))}
                </div>
              </div>
            ))}
          </div>

          {staff.length === 0 && !isLoading && (
            <div className="text-xs text-slate-500 text-center py-6">
              Workforce is currently empty. Click "Seed Demo Data" to load 24 certified clinicians.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
