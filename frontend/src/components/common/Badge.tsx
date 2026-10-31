import React from 'react';
import { cn } from '../../utils/cn';
import type { BurnoutRiskLevel, ShiftType } from '../../types';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'default' | 'outline' | 'risk' | 'shift' | 'skill';
  riskLevel?: BurnoutRiskLevel;
  shiftType?: ShiftType;
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'default',
  riskLevel,
  shiftType,
  className,
}) => {
  let styleClasses = 'bg-slate-800 text-slate-300 border-slate-700';

  if (variant === 'risk' && riskLevel) {
    switch (riskLevel) {
      case 'LOW':
        styleClasses = 'bg-emerald-950/70 text-emerald-300 border-emerald-600/50';
        break;
      case 'MODERATE':
        styleClasses = 'bg-amber-950/70 text-amber-300 border-amber-600/50';
        break;
      case 'HIGH':
        styleClasses = 'bg-orange-950/70 text-orange-300 border-orange-600/50';
        break;
      case 'CRITICAL':
        styleClasses = 'bg-rose-950/80 text-rose-300 border-rose-600/60 font-semibold animate-pulse';
        break;
    }
  } else if (variant === 'shift' && shiftType) {
    switch (shiftType) {
      case 'MORNING':
        styleClasses = 'bg-cyan-950/70 text-cyan-300 border-cyan-600/50';
        break;
      case 'EVENING':
        styleClasses = 'bg-amber-950/70 text-amber-300 border-amber-600/50';
        break;
      case 'NIGHT':
        styleClasses = 'bg-indigo-950/70 text-indigo-300 border-indigo-600/50';
        break;
    }
  } else if (variant === 'skill') {
    styleClasses = 'bg-teal-950/60 text-teal-300 border-teal-700/40 font-mono text-[11px]';
  } else if (variant === 'outline') {
    styleClasses = 'bg-transparent text-slate-400 border-slate-700';
  }

  return (
    <span
      className={cn(
        'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border transition-colors',
        styleClasses,
        className
      )}
    >
      {children}
    </span>
  );
};
