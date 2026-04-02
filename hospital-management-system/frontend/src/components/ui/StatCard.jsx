export default function StatCard({ icon, label, value, color = 'blue', sub }) {
  const colors = {
    blue:'bg-blue-50 text-blue-600',green:'bg-green-50 text-green-600',
    red:'bg-red-50 text-red-600',yellow:'bg-yellow-50 text-yellow-600',
    purple:'bg-purple-50 text-purple-600',orange:'bg-orange-50 text-orange-600',
    pink:'bg-pink-50 text-pink-600',teal:'bg-teal-50 text-teal-600',
  };
  return (
    <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
      <div className="flex items-center gap-4">
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-xl ${colors[color]}`}>
          {icon}
        </div>
        <div>
          <p className="text-sm text-gray-500 font-medium">{label}</p>
          <p className="text-2xl font-bold text-gray-800">{value ?? '—'}</p>
          {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
        </div>
      </div>
    </div>
  );
}
