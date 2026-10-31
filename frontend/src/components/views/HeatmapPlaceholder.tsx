import React from 'react';
import { BrainCircuit } from 'lucide-react';
import type { Employee } from '../../types';

interface HeatmapPlaceholderProps {
  staff: Employee[];
}

export const HeatmapPlaceholder: React.FC<HeatmapPlaceholderProps> = ({ staff }) => {
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-pink-800/40 bg-gradient-to-r from-pink-950/40 via-slate-900/60 to-slate-950 p-6">
        <div className="space-y-1">
          <div className="flex items-center space-x-2">
            <BrainCircuit className="h-6 w-6 text-pink-400" />
            <h2 className="text-xl font-bold text-white">Cognitive Load & Burnout Heatmap</h2>
            <span className="rounded-full bg-pink-500/10 px-2.5 py-0.5 text-xs font-semibold text-pink-400 border border-pink-500/20">
              Day 13 Module
            </span>
          </div>
          <p className="text-sm text-slate-400 max-w-2xl">
            Preview of Day 13's color-coded circadian intensity matrix. Every cell maps the composite
            cognitive load formula: {'L = Duration × Circadian × DeptStress × (1 + Fatigue)'}.
          </p>
        </div>
      </div>

      {/* Heatmap Matrix Preview */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/40 p-5 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-semibold text-white text-sm">Circadian Strain Matrix (7-Day Rolling)</h3>
          <div className="flex items-center space-x-3 text-xs text-slate-400">
            <span className="flex items-center gap-1">
              <span className="h-3 w-3 rounded bg-emerald-500/80" /> Low (&lt;12)
            </span>
            <span className="flex items-center gap-1">
              <span className="h-3 w-3 rounded bg-amber-500/80" /> Moderate (12-18)
            </span>
            <span className="flex items-center gap-1">
              <span className="h-3 w-3 rounded bg-orange-500/80" /> High (18-25)
            </span>
            <span className="flex items-center gap-1">
              <span className="h-3 w-3 rounded bg-rose-500/80" /> Critical (&gt;25)
            </span>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400">
                <th className="pb-3 font-semibold min-w-[160px]">Clinician</th>
                {days.map((day) => (
                  <th key={day} className="pb-3 text-center font-semibold min-w-[70px]">
                    {day}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {staff.slice(0, 10).map((emp, i) => (
                <tr key={emp.id} className="hover:bg-slate-800/20 transition">
                  <td className="py-2.5 font-medium text-slate-300">{emp.name}</td>
                  {days.map((_, dayIdx) => {
                    const mockVal = ((i * 3 + dayIdx * 5) % 30);
                    let cellColor = 'bg-emerald-950/60 text-emerald-400 border-emerald-800/40';
                    if (mockVal > 24) {
                      cellColor = 'bg-rose-950/70 text-rose-300 border-rose-700/50 font-bold';
                    } else if (mockVal > 17) {
                      cellColor = 'bg-orange-950/70 text-orange-300 border-orange-700/50';
                    } else if (mockVal > 11) {
                      cellColor = 'bg-amber-950/70 text-amber-300 border-amber-700/50';
                    }

                    return (
                      <td key={dayIdx} className="py-2.5 px-1 text-center">
                        <div
                          className={`rounded-lg py-1.5 px-2 border text-[11px] font-mono ${cellColor}`}
                        >
                          {mockVal.toFixed(1)}
                        </div>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
