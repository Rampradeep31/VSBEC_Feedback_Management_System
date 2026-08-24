import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar({ title }) {
  const { user, role, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="bg-white border-b border-gray-100 px-6 py-4 flex items-center justify-between shadow-sm">
      <div>
        <p className="text-xs uppercase tracking-wider text-teal-700 font-semibold">V.S.B. Engineering College, Karur</p>
        <h1 className="text-lg font-bold text-slate-800">{title}</h1>
      </div>
      <div className="flex items-center gap-4">
        {user && (
          <span className="text-sm text-slate-600 font-medium">
            {role === 'ADMIN' ? user.name : `Reg No: ${user.name}`}
          </span>
        )}
        {role === 'ADMIN' && (
          <button
            onClick={() => navigate('/admin/change-password')}
            className="text-sm bg-teal-500 hover:bg-teal-600 text-teal-900 hover:text-teal-950 transition-colors px-3 py-1.5 rounded-md font-semibold"
          >
            Change Password
          </button>
        )}
        <button
          onClick={handleLogout}
          className="text-sm bg-slate-100 hover:bg-slate-200 text-slate-700 transition-colors px-3 py-1.5 rounded-md font-semibold"
        >
          Logout
        </button>
      </div>
    </header>
  )
}
