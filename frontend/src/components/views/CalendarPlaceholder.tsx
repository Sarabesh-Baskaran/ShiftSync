import React from 'react';
import { CalendarDays } from 'lucide-react';
import type { Shift } from '../../types';
import { Badge } from '../common/Badge';

interface CalendarPlaceholderProps {
  shifts: Shift[];
}

export const CalendarPlaceholder: React.FC<CalendarPlaceholderProps> = ({ shifts }) => {
  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-cyan-800/40 bg-gradient-to-r from-cyan-950/40 via-slate-900/60 to-slate-950 p-6">
        <div className="flex items-start justify-between">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <CalendarDays className="h-6 w-6 text-cyan-400" />
              <h2 className="text-xl font-bold text-white">Hospital Roster Calendar</h2>
              <span className="rounded-full bg-cyan-500/10 px-2.5 py-0.5 text-xs font-semibold text-cyan-400 border border-cyan-500/20">
                Day 12 Module
              </span>
            </div>
            <p className="text-sm text-slate-400 max-w-2xl">
              On Day 12, we will integrate FullCalendar with custom event rendering for Morning, Evening,
              and Night shifts, color-coded by clinical department and assigned clinician with drag-and-drop
              reassignment.
            </p>
          </div>
        </div>
      </div>

      {/* Shifts Summary Card */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-5 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-semibold text-white text-sm">Active Roster Shifts ({shifts.length})</h3>
          <span className="text-xs text-slate-500 font-mono">
            {shifts.filter((s) => s.status === 'ASSIGNED').length} Assigned •{' '}
            {shifts.filter((s) => s.status === 'UNASSIGNED').length} Unassigned
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 max-h-[500px] overflow-y-auto pr-2">
          {shifts.slice(0, 18).map((shift) => (
            <div
              key={shift.id}
              className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/70 hover:border-slate-700 transition space-y-2"
            >
              <div className="flex items-center justify-between">
                <Badge variant="shift" shiftType={shift.shiftType}>
                  {shift.shiftType}
                </Badge>
                <span className="text-[11px] font-mono text-slate-400">
                  {shift.shiftDate}
                </span>
              </div>
              <div className="text-sm font-medium text-slate-200">
                {shift.department.name}
              </div>
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>{shift.startTime} - {shift.endTime}</span>
                <span className={shift.assignedEmployee ? 'text-emerald-400 font-medium' : 'text-amber-400'}>
                  {shift.assignedEmployee ? shift.assignedEmployee.name : 'Unassigned'}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
