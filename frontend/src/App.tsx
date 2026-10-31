import { useState, useEffect, useCallback } from 'react';
import { Navbar } from './components/layout/Navbar';
import { Sidebar } from './components/layout/Sidebar';
import type { ActiveTab } from './components/layout/Sidebar';
import { HeaderStats } from './components/layout/HeaderStats';
import { OverviewTab } from './components/views/OverviewTab';
import { CalendarPlaceholder } from './components/views/CalendarPlaceholder';
import { HeatmapPlaceholder } from './components/views/HeatmapPlaceholder';
import { SolverView } from './components/views/SolverView';
import { StaffView } from './components/views/StaffView';
import { AnalyticsView } from './components/views/AnalyticsView';

import type { Department, Employee, Shift, TeamScheduleAnalyticsDTO } from './types';
import { departmentService } from './services/departmentService';
import { staffService } from './services/staffService';
import { shiftService } from './services/shiftService';
import { analyticsService } from './services/analyticsService';
import { demoService } from './services/demoService';
import type { DatabaseStatus } from './services/demoService';
import { CheckCircle2, AlertCircle, X } from 'lucide-react';

export function App() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('overview');
  const [departments, setDepartments] = useState<Department[]>([]);
  const [staff, setStaff] = useState<Employee[]>([]);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [teamAnalytics, setTeamAnalytics] = useState<TeamScheduleAnalyticsDTO | null>(null);
  const [dbStatus, setDbStatus] = useState<DatabaseStatus | undefined>(undefined);

  const [backendConnected, setBackendConnected] = useState<boolean>(true);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSeeding, setIsSeeding] = useState<boolean>(false);
  const [notification, setNotification] = useState<{
    type: 'success' | 'error' | 'info';
    message: string;
  } | null>(null);

  const showNotification = (message: string, type: 'success' | 'error' | 'info' = 'success') => {
    setNotification({ type, message });
    setTimeout(() => {
      setNotification((curr) => (curr?.message === message ? null : curr));
    }, 5000);
  };

  const loadAllData = useCallback(async () => {
    setIsLoading(true);
    try {
      const today = new Date().toISOString().split('T')[0];
      const nextWeek = new Date(Date.now() + 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

      // 1. Fetch DB Status & Health
      const status = await demoService.getStatus();
      setDbStatus(status);
      setBackendConnected(true);

      // 2. Fetch Core Entities
      const [deptData, staffData, shiftData] = await Promise.all([
        departmentService.getDepartments().catch(() => []),
        staffService.getAllStaff().catch(() => []),
        shiftService.getShifts(today, nextWeek).catch(() => []),
      ]);

      setDepartments(deptData);
      setStaff(staffData);
      setShifts(shiftData);

      // 3. Fetch Analytics
      if (staffData.length > 0) {
        const analytics = await analyticsService.getTeamAnalytics(today, nextWeek).catch(() => null);
        setTeamAnalytics(analytics);
      }
    } catch (err: unknown) {
      console.warn('Backend currently unreachable on port 8080:', err);
      setBackendConnected(false);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAllData();
  }, [loadAllData]);

  const handleSeedDemo = async () => {
    setIsSeeding(true);
    try {
      await demoService.seedData(true);
      showNotification('Hospital demo dataset initialized (3 departments, 24 staff, 189 shifts)!');
      await loadAllData();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to seed demo data.';
      showNotification(msg, 'error');
    } finally {
      setIsSeeding(false);
    }
  };

  const handleResetData = async () => {
    if (!window.confirm('Are you sure you want to clear all shifts, staff, and check-ins?')) {
      return;
    }
    setIsSeeding(true);
    try {
      await demoService.resetData();
      showNotification('All ShiftSync database tables cleared.', 'info');
      await loadAllData();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to reset data.';
      showNotification(msg, 'error');
    } finally {
      setIsSeeding(false);
    }
  };

  const assignedShiftsCount = shifts.filter((s) => s.status === 'ASSIGNED').length;

  return (
    <div className="min-h-dvh flex flex-col bg-slate-950 text-slate-100 font-sans antialiased">
      {/* Toast Notification */}
      {notification && (
        <div className="fixed bottom-5 right-5 z-50 flex items-center space-x-3 rounded-2xl bg-slate-900 border border-slate-700 p-4 shadow-2xl backdrop-blur-md">
          {notification.type === 'success' && <CheckCircle2 className="h-5 w-5 text-emerald-400" />}
          {notification.type === 'error' && <AlertCircle className="h-5 w-5 text-rose-400" />}
          {notification.type === 'info' && <CheckCircle2 className="h-5 w-5 text-cyan-400" />}
          <span className="text-sm font-medium text-slate-200">{notification.message}</span>
          <button
            onClick={() => setNotification(null)}
            className="text-slate-500 hover:text-slate-300 ml-2 cursor-pointer"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Main Top Navbar */}
      <Navbar
        backendConnected={backendConnected}
        isSeeding={isSeeding}
        onSeedDemo={handleSeedDemo}
        onResetData={handleResetData}
        statsSummary={dbStatus}
      />

      {/* App Workspace Body: Sidebar + Main Content */}
      <div className="flex flex-1 overflow-hidden">
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />

        <main className="flex-1 flex flex-col overflow-y-auto bg-gradient-to-b from-slate-950 via-slate-900/20 to-slate-950">
          {/* Real-time KPI Metric Cards */}
          <HeaderStats
            dbStatus={dbStatus}
            teamAnalytics={teamAnalytics}
            assignedShiftsCount={assignedShiftsCount}
            totalShiftsCount={shifts.length}
          />

          {/* Module View Content */}
          <div className="flex-1 p-6 pt-3">
            {isLoading && (
              <div className="text-center py-6 text-slate-500 text-xs font-mono animate-pulse">
                Synchronizing hospital entities with ShiftSync backend...
              </div>
            )}

            {activeTab === 'overview' && (
              <OverviewTab
                departments={departments}
                staff={staff}
                shifts={shifts}
                onNavigate={setActiveTab}
                isLoading={isLoading}
              />
            )}

            {activeTab === 'calendar' && (
              <CalendarPlaceholder shifts={shifts} />
            )}

            {activeTab === 'heatmap' && (
              <HeatmapPlaceholder staff={staff} />
            )}

            {activeTab === 'solver' && (
              <SolverView onSolveComplete={loadAllData} />
            )}

            {activeTab === 'staff' && (
              <StaffView staff={staff} departments={departments} />
            )}

            {activeTab === 'analytics' && (
              <AnalyticsView />
            )}
          </div>
        </main>
      </div>
    </div>
  );
}

export default App;
