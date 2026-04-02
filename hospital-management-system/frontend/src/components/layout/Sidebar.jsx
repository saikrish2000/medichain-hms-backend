import { NavLink, useNavigate } from 'react-router-dom';
import useAuthStore from '../../store/authStore';

const NAV = {
  ADMIN: [
    { to:'/admin/dashboard',   icon:'🏠', label:'Dashboard' },
    { to:'/admin/approvals',   icon:'✅', label:'Approvals' },
    { to:'/admin/patients',    icon:'👥', label:'Patients' },
    { to:'/admin/departments', icon:'🏥', label:'Departments' },
    { to:'/admin/users',       icon:'👤', label:'Users' },
    { to:'/admin/audit-logs',  icon:'📋', label:'Audit Logs' },
    { to:'/admin/reports',     icon:'📊', label:'Reports' },
    { to:'/billing/dashboard', icon:'💰', label:'Billing' },
    { to:'/blood-bank/dashboard', icon:'🩸', label:'Blood Bank' },
    { to:'/ambulance/dashboard',  icon:'🚑', label:'Ambulance' },
  ],
  DOCTOR: [
    { to:'/doctor/dashboard',     icon:'🏠', label:'Dashboard' },
    { to:'/doctor/appointments',  icon:'📅', label:'Appointments' },
    { to:'/doctor/patients',      icon:'👥', label:'My Patients' },
    { to:'/doctor/prescriptions', icon:'💊', label:'Prescriptions' },
    { to:'/doctor/lab-orders',    icon:'🔬', label:'Lab Orders' },
    { to:'/doctor/slots',         icon:'🕐', label:'Manage Slots' },
  ],
  PATIENT: [
    { to:'/patient/dashboard',    icon:'🏠', label:'Dashboard' },
    { to:'/appointments/book',    icon:'📅', label:'Book Appointment' },
    { to:'/patient/appointments', icon:'🗓', label:'My Appointments' },
    { to:'/patient/records',      icon:'📋', label:'Medical Records' },
    { to:'/patient/vitals',       icon:'❤️', label:'Vitals' },
    { to:'/billing/my-bills',     icon:'💰', label:'My Bills' },
  ],
  NURSE: [
    { to:'/nurse/dashboard', icon:'🏠', label:'Dashboard' },
    { to:'/nurse/patients',  icon:'👥', label:'Patients' },
    { to:'/nurse/tasks',     icon:'✅', label:'Tasks' },
    { to:'/nurse/vitals',    icon:'❤️', label:'Record Vitals' },
    { to:'/nurse/emar',      icon:'💊', label:'eMAR' },
    { to:'/nurse/handover',  icon:'🔄', label:'Handover' },
  ],
  INDEPENDENT_NURSE: [
    { to:'/nurse/dashboard', icon:'🏠', label:'Dashboard' },
    { to:'/nurse/patients',  icon:'👥', label:'Patients' },
    { to:'/nurse/tasks',     icon:'✅', label:'Tasks' },
    { to:'/nurse/emar',      icon:'💊', label:'eMAR' },
  ],
  PHARMACIST: [
    { to:'/pharmacy/dashboard',     icon:'🏠', label:'Dashboard' },
    { to:'/pharmacy/medicines',     icon:'💊', label:'Medicines' },
    { to:'/pharmacy/prescriptions', icon:'📋', label:'Prescriptions' },
  ],
  LAB_TECHNICIAN: [
    { to:'/lab/dashboard', icon:'🏠', label:'Dashboard' },
    { to:'/lab/orders',    icon:'📋', label:'Lab Orders' },
    { to:'/lab/tests',     icon:'🔬', label:'Test Catalog' },
  ],
  PHLEBOTOMIST: [
    { to:'/lab/dashboard', icon:'🏠', label:'Dashboard' },
    { to:'/lab/orders',    icon:'📋', label:'Sample Collection' },
  ],
  BLOOD_BANK_MANAGER: [
    { to:'/blood-bank/dashboard', icon:'🏠', label:'Dashboard' },
    { to:'/blood-bank/inventory', icon:'🩸', label:'Inventory' },
  ],
  AMBULANCE_OPERATOR: [
    { to:'/ambulance/dashboard', icon:'🏠', label:'Dashboard' },
    { to:'/ambulance/dispatch',  icon:'📡', label:'Dispatch' },
    { to:'/ambulance/calls',     icon:'📞', label:'Calls' },
    { to:'/ambulance/fleet',     icon:'🚑', label:'Fleet' },
  ],
  RECEPTIONIST: [
    { to:'/receptionist/dashboard',    icon:'🏠', label:'Dashboard' },
    { to:'/receptionist/appointments', icon:'📅', label:'Appointments' },
    { to:'/billing/dashboard',         icon:'💰', label:'Billing' },
  ],
};

export default function Sidebar({ open, onClose }) {
  const { user } = useAuthStore();
  const links = NAV[user?.role] || [];

  return (
    <>
      {/* Overlay */}
      {!open && <div className="hidden"/>}
      <aside className={`${open?'w-64':'w-0 overflow-hidden'} transition-all duration-300 bg-white border-r border-gray-200 flex flex-col shrink-0`}>
        {/* Logo */}
        <div className="h-16 flex items-center px-6 border-b border-gray-200">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center mr-3">
            <span className="text-white text-sm font-bold">M</span>
          </div>
          <span className="font-bold text-gray-800">MediChain</span>
        </div>
        {/* Nav */}
        <nav className="flex-1 overflow-y-auto py-4 px-3">
          {links.map(({to,icon,label}) => (
            <NavLink key={to} to={to}
              className={({isActive}) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl mb-1 text-sm font-medium transition
                ${isActive
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-800'}`
              }
            >
              <span className="text-base">{icon}</span>
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
        {/* User */}
        <div className="p-4 border-t border-gray-200">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-semibold">
              {user?.fullName?.[0] || 'U'}
            </div>
            <div className="overflow-hidden">
              <p className="text-sm font-semibold text-gray-800 truncate">{user?.fullName}</p>
              <p className="text-xs text-gray-500 truncate">{user?.email}</p>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
