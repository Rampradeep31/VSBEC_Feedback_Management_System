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
    <header className="bg-navy-700 text-white px-6 py-4 flex items-center justify-between shadow-md">
      <div>
        <p className="text-xs uppercase tracking-wider text-teal-100/80">V.S.B. Engineering College, Karur</p>
        <h1 className="text-lg font-semibold">{title}</h1>
      </div>
      <div className="flex items-center gap-4">
        {user && (
          <span className="text-sm text-navy-50">
            {role === 'ADMIN' ? user.name : `Reg No: ${user.name}`}
          </span>
        )}
        <button
          onClick={handleLogout}
          className="text-sm bg-navy-600 hover:bg-navy-500 transition-colors px-3 py-1.5 rounded-md"
        >
          Logout
        </button>
      </div>
    </header>
  )
}
