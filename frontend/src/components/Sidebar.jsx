import React from 'react'
import { NavLink } from 'react-router-dom'

const links = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/admin/academic-years', label: 'Academic Years', icon: '📅' },
  { to: '/admin/departments', label: 'Departments', icon: '🏛️' },
  { to: '/admin/classes', label: 'Classes', icon: '🎓' },
  { to: '/admin/faculty', label: 'Faculty', icon: '👨‍🏫' },
  { to: '/admin/subjects', label: 'Subjects', icon: '📚' },
  { to: '/admin/students', label: 'Students', icon: '🧑‍🎓' },
  { to: '/admin/feedback-control', label: 'Feedback Control', icon: '🔓' },
  { to: '/admin/reports', label: 'Reports', icon: '📄' },
]

export default function Sidebar() {
  return (
    <aside className="w-60 bg-navy-800 text-navy-50 min-h-screen py-6 px-3 hidden md:block">
      <nav className="flex flex-col gap-1">
        {links.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive ? 'bg-teal-600 text-white' : 'hover:bg-navy-700 text-navy-100'
              }`
            }
          >
            <span>{l.icon}</span>
            {l.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
