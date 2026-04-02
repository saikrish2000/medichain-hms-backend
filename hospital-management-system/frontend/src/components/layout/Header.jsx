import { useNavigate } from 'react-router-dom';
import useAuthStore from '../../store/authStore';
import toast from 'react-hot-toast';

export default function Header({ onMenuClick }) {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    toast.success('Logged out successfully');
    navigate('/login');
  };

  const roleColors = {
    ADMIN:'bg-purple-100 text-purple-700', DOCTOR:'bg-blue-100 text-blue-700',
    PATIENT:'bg-green-100 text-green-700', NURSE:'bg-pink-100 text-pink-700',
    PHARMACIST:'bg-yellow-100 text-yellow-700', LAB_TECHNICIAN:'bg-orange-100 text-orange-700',
    BLOOD_BANK_MANAGER:'bg-red-100 text-red-700', AMBULANCE_OPERATOR:'bg-teal-100 text-teal-700',
    RECEPTIONIST:'bg-indigo-100 text-indigo-700',
  };

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shrink-0 z-10">
      <div className="flex items-center gap-4">
        <button onClick={onMenuClick} className="p-2 rounded-lg hover:bg-gray-100 transition">
          <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16"/>
          </svg>
        </button>
        <h1 className="text-lg font-semibold text-gray-800">MediChain HMS</h1>
      </div>
      <div className="flex items-center gap-3">
        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${roleColors[user?.role] || 'bg-gray-100 text-gray-600'}`}>
          {user?.role?.replace(/_/g,' ')}
        </span>
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-semibold">
            {user?.fullName?.[0] || user?.username?.[0] || 'U'}
          </div>
          <span className="text-sm font-medium text-gray-700 hidden md:block">{user?.fullName || user?.username}</span>
        </div>
        <button onClick={handleLogout}
          className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 rounded-lg transition font-medium">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
          </svg>
          Logout
        </button>
      </div>
    </header>
  );
}
