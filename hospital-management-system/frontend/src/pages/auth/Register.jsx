import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useAuthStore from '../../store/authStore';
import api from '../../api/axios';
import toast from 'react-hot-toast';

const ROLES = [
  { value:'PATIENT',           label:'Patient' },
  { value:'DOCTOR',            label:'Doctor' },
  { value:'NURSE',             label:'Nurse' },
  { value:'PHARMACIST',        label:'Pharmacist' },
  { value:'LAB_TECHNICIAN',    label:'Lab Technician' },
  { value:'RECEPTIONIST',      label:'Receptionist' },
  { value:'BLOOD_BANK_MANAGER',label:'Blood Bank Manager' },
  { value:'AMBULANCE_OPERATOR',label:'Ambulance Operator' },
];

export default function Register() {
  const [form, setForm] = useState({
    firstName:'', lastName:'', username:'', email:'',
    password:'', confirmPassword:'', role:'PATIENT', phone:''
  });
  const [loading, setLoading] = useState(false);
  const { login } = useAuthStore();
  const navigate = useNavigate();

  const set = (k,v) => setForm(f=>({...f,[k]:v}));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) { toast.error('Passwords do not match'); return; }
    setLoading(true);
    try {
      const { data } = await api.post('/auth/register', form);
      login(data);
      toast.success('Account created successfully!');
      const routes = { ADMIN:'admin',DOCTOR:'doctor',PATIENT:'patient',NURSE:'nurse',
        PHARMACIST:'pharmacy',LAB_TECHNICIAN:'lab',BLOOD_BANK_MANAGER:'blood-bank',
        AMBULANCE_OPERATOR:'ambulance',RECEPTIONIST:'receptionist' };
      navigate(`/${routes[data.role]||'patient'}/dashboard`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 bg-blue-600 rounded-2xl shadow-lg mb-3">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4"/>
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Create Account</h1>
          <p className="text-gray-500">MediChain Hospital Management System</p>
        </div>

        <div className="bg-white rounded-3xl shadow-xl p-8 border border-gray-100">
          <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-5">
            {[['firstName','First Name'],['lastName','Last Name'],['username','Username'],['email','Email'],['phone','Phone']].map(([k,label]) => (
              <div key={k} className={k==='email'||k==='phone' ? 'col-span-2 md:col-span-1' : ''}>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
                <input
                  type={k==='email'?'email':'text'} required={k!=='phone'}
                  className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                  value={form[k]} onChange={e=>set(k,e.target.value)}
                />
              </div>
            ))}
            <div className="col-span-2 md:col-span-1">
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Role</label>
              <select
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                value={form.role} onChange={e=>set('role',e.target.value)}
              >
                {ROLES.map(r=><option key={r.value} value={r.value}>{r.label}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Password</label>
              <input type="password" required
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                value={form.password} onChange={e=>set('password',e.target.value)}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Confirm Password</label>
              <input type="password" required
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                value={form.confirmPassword} onChange={e=>set('confirmPassword',e.target.value)}
              />
            </div>
            <button type="submit" disabled={loading}
              className="col-span-2 py-3 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 active:scale-95 transition disabled:opacity-60 shadow-sm"
            >
              {loading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>
          <p className="text-center text-sm text-gray-500 mt-5">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-600 font-medium hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
