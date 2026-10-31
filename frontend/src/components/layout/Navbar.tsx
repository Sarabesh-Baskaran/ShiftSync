import React from 'react';
import { Activity, Database, RefreshCw, AlertCircle, ShieldCheck } from 'lucide-react';
import { cn } from '../../utils/cn';

interface NavbarProps {
  backendConnected: boolean;
  isSeeding: boolean;
  onSeedDemo: () => Promise<void>;
  onResetData: () => Promise<void>;
  statsSummary?: {
    departments: number;
    staff: number;
    shifts: number;
  };
}

export const Navbar: React.FC<NavbarProps> = ({
  backendConnected,
  isSeeding,
  onSeedDemo,
  onResetData,
  statsSummary,
}) => {
  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800 bg-slate-950/85 backdrop-blur-md px-6 py-3.5 transition-all">
      <div className="flex items-center justify-between">
        {/* Brand & Mission Title */}
        <div className="flex items-center space-x-3.5">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-tr from-teal-600 to-cyan-500 shadow-lg shadow-teal-500/20">
            <Activity className="h-6 w-6 text-white animate-pulse" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xl font-bold tracking-tight text-white font-sans">
                Shift<span className="text-teal-400">Sync</span>
              </span>
              <span className="rounded-md bg-teal-950/80 px-2 py-0.5 text-[11px] font-semibold text-teal-400 border border-teal-700/50">
                AI CLINICAL SCHEDULER
              </span>
            </div>
            <p className="text-xs text-slate-400">
              Cognitive Load Optimization & Empathetic Healthcare Rostering
            </p>
          </div>
        </div>

        {/* System Status & Actions */}
        <div className="flex items-center space-x-4">
          {/* Backend Connection Indicator */}
          <div
            className={cn(
              'flex items-center space-x-2 px-3 py-1 rounded-full text-xs font-medium border transition-colors',
              backendConnected
                ? 'bg-emerald-950/40 text-emerald-400 border-emerald-800/50'
                : 'bg-rose-950/40 text-rose-400 border-rose-800/50'
            )}
            title={backendConnected ? 'Backend REST API connected' : 'Cannot reach http://localhost:8080'}
          >
            {backendConnected ? (
              <>
                <span className="h-2 w-2 rounded-full bg-emerald-400 animate-ping" />
                <span className="flex items-center gap-1">
                  <ShieldCheck className="h-3.5 w-3.5 text-emerald-400" />
                  API Active
                </span>
              </>
            ) : (
              <>
                <span className="h-2 w-2 rounded-full bg-rose-500" />
                <span className="flex items-center gap-1">
                  <AlertCircle className="h-3.5 w-3.5 text-rose-400" />
                  API Offline (port 8080)
                </span>
              </>
            )}
          </div>

          {/* Database Summary Pills */}
          {statsSummary && (
            <div className="hidden lg:flex items-center space-x-2 text-xs text-slate-400 bg-slate-900/80 px-3 py-1.5 rounded-lg border border-slate-800">
              <Database className="h-3.5 w-3.5 text-slate-400" />
              <span>
                <strong className="text-slate-200">{statsSummary.staff}</strong> Staff
              </span>
              <span className="text-slate-600">•</span>
              <span>
                <strong className="text-slate-200">{statsSummary.shifts}</strong> Shifts
              </span>
              <span className="text-slate-600">•</span>
              <span>
                <strong className="text-slate-200">{statsSummary.departments}</strong> Depts
              </span>
            </div>
          )}

          {/* Action: Seed Demo Data */}
          <button
            onClick={onSeedDemo}
            disabled={isSeeding || !backendConnected}
            className="flex items-center space-x-1.5 rounded-lg bg-teal-600 hover:bg-teal-500 disabled:bg-slate-800 disabled:text-slate-500 px-3.5 py-1.5 text-xs font-semibold text-white shadow-md shadow-teal-900/30 transition active:scale-95 cursor-pointer"
          >
            <RefreshCw className={cn('h-3.5 w-3.5', isSeeding && 'animate-spin')} />
            <span>{isSeeding ? 'Seeding Hospital...' : 'Seed Demo Data'}</span>
          </button>

          {/* Action: Reset */}
          <button
            onClick={onResetData}
            disabled={isSeeding || !backendConnected}
            className="rounded-lg bg-slate-800 hover:bg-slate-700 disabled:bg-slate-900 disabled:text-slate-600 px-3 py-1.5 text-xs font-medium text-slate-300 border border-slate-700/60 transition cursor-pointer"
            title="Reset database to empty state"
          >
            Clear Data
          </button>
        </div>
      </div>
    </header>
  );
};
