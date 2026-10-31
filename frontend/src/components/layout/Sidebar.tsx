import React from 'react';
import {
  LayoutDashboard,
  CalendarDays,
  BrainCircuit,
  Cpu,
  Users2,
  BarChart3,
  HeartPulse,
  Layers,
} from 'lucide-react';
import { cn } from '../../utils/cn';

export type ActiveTab = 'overview' | 'calendar' | 'heatmap' | 'solver' | 'staff' | 'analytics';

interface SidebarProps {
  activeTab: ActiveTab;
  onTabChange: (tab: ActiveTab) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  onTabChange,
}) => {
  const navItems = [
    {
      id: 'overview' as ActiveTab,
      label: 'Operations Center',
      icon: LayoutDashboard,
      badge: 'Day 11',
      description: 'Hospital overview & stats',
    },
    {
      id: 'calendar' as ActiveTab,
      label: 'Roster Calendar',
      icon: CalendarDays,
      badge: 'Day 12',
      description: 'Department shift schedule',
    },
    {
      id: 'heatmap' as ActiveTab,
      label: 'Cognitive Load Heatmap',
      icon: BrainCircuit,
      badge: 'Day 13',
      description: 'Circadian & fatigue analysis',
    },
    {
      id: 'solver' as ActiveTab,
      label: 'AI Solver Studio',
      icon: Cpu,
      badge: 'Day 14',
      description: 'Timefold constraint engine',
      highlight: true,
    },
    {
      id: 'staff' as ActiveTab,
      label: 'Staff & Wellbeing',
      icon: Users2,
      badge: 'Day 15',
      description: 'Clinicians & daily check-ins',
    },
    {
      id: 'analytics' as ActiveTab,
      label: 'Capacity & Fairness',
      icon: BarChart3,
      badge: 'Day 4 & 9',
      description: 'Load variance & bottlenecks',
    },
  ];

  return (
    <aside className="w-64 border-r border-slate-800 bg-slate-950/70 p-4 flex flex-col justify-between shrink-0">
      <div className="space-y-6">
        {/* Navigation Section */}
        <div>
          <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-2">
            Clinical Modules
          </p>
          <nav className="space-y-1.5">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => onTabChange(item.id)}
                  className={cn(
                    'w-full flex items-center justify-between rounded-xl px-3 py-2.5 text-left text-sm font-medium transition-all cursor-pointer group',
                    isActive
                      ? 'bg-teal-600/15 text-teal-300 border border-teal-500/30 shadow-sm'
                      : 'text-slate-400 hover:bg-slate-900/80 hover:text-slate-200'
                  )}
                >
                  <div className="flex items-center space-x-3">
                    <Icon
                      className={cn(
                        'h-5 w-5 transition-colors',
                        isActive ? 'text-teal-400' : 'text-slate-500 group-hover:text-slate-300'
                      )}
                    />
                    <div>
                      <div className="leading-tight">{item.label}</div>
                      <div className="text-[11px] text-slate-500 font-normal leading-tight">
                        {item.description}
                      </div>
                    </div>
                  </div>
                  <span
                    className={cn(
                      'text-[10px] px-1.5 py-0.5 rounded font-mono',
                      isActive
                        ? 'bg-teal-900/50 text-teal-300'
                        : 'bg-slate-800/80 text-slate-500'
                    )}
                  >
                    {item.badge}
                  </span>
                </button>
              );
            })}
          </nav>
        </div>

        {/* Clinical Guardrails Info Card */}
        <div className="rounded-xl border border-slate-800/80 bg-slate-900/50 p-3.5 space-y-2.5">
          <div className="flex items-center space-x-2 text-xs font-semibold text-slate-300">
            <HeartPulse className="h-4 w-4 text-teal-400" />
            <span>Timefold Safety Invariants</span>
          </div>
          <ul className="space-y-1.5 text-[11px] text-slate-400">
            <li className="flex items-center space-x-1.5">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 shrink-0" />
              <span>&ge; 12h Rest Gap ("Anti-Clopening")</span>
            </li>
            <li className="flex items-center space-x-1.5">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 shrink-0" />
              <span>Strict 40h Weekly Hours Cap</span>
            </li>
            <li className="flex items-center space-x-1.5">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 shrink-0" />
              <span>Required Clinical Qualifications</span>
            </li>
            <li className="flex items-center space-x-1.5">
              <span className="h-1.5 w-1.5 rounded-full bg-amber-400 shrink-0" />
              <span>Max 2 Consecutive ICU/ER Shifts</span>
            </li>
          </ul>
        </div>
      </div>

      {/* Footer Info */}
      <div className="pt-4 border-t border-slate-900/80">
        <div className="flex items-center justify-between text-[11px] text-slate-500">
          <span>Engine: Timefold 1.15.0</span>
          <span className="flex items-center gap-1 text-teal-400">
            <Layers className="h-3 w-3" />
            Hard: 0 Violations
          </span>
        </div>
      </div>
    </aside>
  );
};
