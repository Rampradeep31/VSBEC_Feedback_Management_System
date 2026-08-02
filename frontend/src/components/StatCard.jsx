import React from 'react'

export default function StatCard({ label, value, accent = 'navy', suffix = '' }) {
  const accentClasses = {
    navy: 'text-navy-600 bg-navy-50',
    teal: 'text-teal-700 bg-teal-50',
    amber: 'text-amber-700 bg-amber-50',
    rose: 'text-rose-700 bg-rose-50',
  }[accent]

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 flex flex-col gap-2">
      <span className="text-sm text-gray-500 font-medium">{label}</span>
      <span className={`text-3xl font-bold ${accentClasses.split(' ')[0]}`}>
        {value}{suffix}
      </span>
    </div>
  )
}
