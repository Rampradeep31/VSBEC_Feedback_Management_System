import React, { createContext, useContext, useState, useCallback } from 'react'
import api from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })
  const [role, setRole] = useState(() => localStorage.getItem('role'))

  const adminLogin = useCallback(async (username, password) => {
    const res = await api.post('/auth/admin/login', { username, password })
    persistSession(res.data)
    return res.data
  }, [])

  const studentLogin = useCallback(async (registerNumber) => {
    const res = await api.post('/auth/student/login', { registerNumber })
    persistSession(res.data)
    return res.data
  }, [])

  function persistSession(data) {
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('user', JSON.stringify(data))
    setUser(data)
    setRole(data.role)
  }

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('user')
    setUser(null)
    setRole(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, role, adminLogin, studentLogin, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
