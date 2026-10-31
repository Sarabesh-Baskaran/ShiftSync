import React, { useState } from 'react';
import { Users2, Search, Filter } from 'lucide-react';
import type { Department, Employee } from '../../types';
import { Badge } from '../common/Badge';

interface StaffViewProps {
  staff: Employee[];
  departments: Department[];
}

export const StaffView: React.FC<StaffViewProps> = ({ staff, departments }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDept, setSelectedDept] = useState<string>('ALL');

  const filteredStaff = staff.filter((emp) => {
    const matchesSearch =
      emp.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      emp.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesDept =
      selectedDept === 'ALL' ||
      emp.primaryDepartment?.id.toString() === selectedDept;
    return matchesSearch && matchesDept;
  });

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-purple-800/40 bg-gradient-to-r from-purple-950/40 via-slate-900/60 to-slate-950 p-6">
        <div className="space-y-1">
          <div className="flex items-center space-x-2">
            <Users2 className="h-6 w-6 text-purple-400" />
            <h2 className="text-xl font-bold text-white">Clinical Workforce Directory</h2>
            <span className="rounded-full bg-purple-500/10 px-2.5 py-0.5 text-xs font-semibold text-purple-400 border border-purple-500/20">
              {staff.length} Clinicians
            </span>
          </div>
          <p className="text-sm text-slate-400 max-w-2xl">
            Directory of licensed physicians, triage nurses, and intensive care specialists. Monitor
            individual skill sets, weekly maximum hours caps, and baseline fatigue factors.
          </p>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bg-slate-900/40 p-3 rounded-2xl border border-slate-800">
        <div className="relative w-full sm:w-72">
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search clinician by name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-purple-500 transition"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto">
          <Filter className="h-4 w-4 text-slate-500 shrink-0" />
          <select
            value={selectedDept}
            onChange={(e) => setSelectedDept(e.target.value)}
            className="bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-purple-500 cursor-pointer w-full sm:w-auto"
          >
            <option value="ALL">All Departments</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id.toString()}>
                {d.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Staff Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredStaff.map((emp) => (
          <div
            key={emp.id}
            className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/80 hover:border-slate-700/80 transition space-y-3"
          >
            <div className="flex items-start justify-between">
              <div>
                <h4 className="font-semibold text-slate-200 text-sm">{emp.name}</h4>
                <p className="text-xs text-slate-400">{emp.email}</p>
              </div>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                {emp.maxWeeklyHours}h cap
              </span>
            </div>

            <div className="text-xs text-slate-400 flex items-center justify-between">
              <span>Department:</span>
              <span className="text-slate-200 font-medium">
                {emp.primaryDepartment?.name || 'Unassigned'}
              </span>
            </div>

            <div className="text-xs text-slate-400 flex items-center justify-between">
              <span>Baseline Fatigue:</span>
              <span className="font-mono text-amber-400 font-semibold">
                {emp.baselineFatigue}x
              </span>
            </div>

            <div className="pt-2 border-t border-slate-800/70">
              <div className="text-[11px] text-slate-500 mb-1.5 font-medium">Clinical Qualifications:</div>
              <div className="flex flex-wrap gap-1">
                {emp.skills?.map((sk) => (
                  <Badge key={sk} variant="skill">
                    {sk.replace('_', ' ')}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
