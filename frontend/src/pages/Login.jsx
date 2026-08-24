import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const [mode, setMode] = useState('student') // 'student' | 'admin'
  const [registerNumber, setRegisterNumber] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { studentLogin, adminLogin } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (mode === 'student') {
        await studentLogin(registerNumber.trim())
        navigate('/student/feedback')
      } else {
        await adminLogin(username.trim(), password)
        navigate('/admin/dashboard')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your details.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden border border-slate-100">
        <div className="bg-gradient-to-r from-teal-500 to-teal-600 text-white px-8 py-6 text-center">
          <p className="text-xs uppercase tracking-widest text-teal-100/90 mb-1">V.S.B. Engineering College, Karur</p>
          <h1 className="text-xl font-bold">Faculty Feedback System</h1>
        </div>

        <div className="flex border-b border-gray-100">
          <button
            className={`flex-1 py-3 text-sm font-semibold transition-colors ${
              mode === 'student' ? 'text-teal-700 border-b-2 border-teal-600' : 'text-gray-400'
            }`}
            onClick={() => { setMode('student'); setError('') }}
          >
            Student Login
          </button>
          <button
            className={`flex-1 py-3 text-sm font-semibold transition-colors ${
              mode === 'admin' ? 'text-teal-700 border-b-2 border-teal-600' : 'text-gray-400'
            }`}
            onClick={() => { setMode('admin'); setError('') }}
          >
            Admin Login
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-8 flex flex-col gap-4">
          {mode === 'student' ? (
            <div>
              <label className="text-sm font-medium text-gray-600">Register Number</label>
              <input
                type="text"
                required
                value={registerNumber}
                onChange={(e) => setRegisterNumber(e.target.value)}
                placeholder="e.g. 812723XXXXXX"
                className="mt-1 w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-teal-500"
              />
            </div>
          ) : (
            <>
              <div>
                <label className="text-sm font-medium text-gray-600">Username</label>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="mt-1 w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-teal-500"
                />
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600">Password</label>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="mt-1 w-full border border-gray-300 rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-teal-500"
                />
              </div>
            </>
          )}

          {error && <p className="text-sm text-rose-600 bg-rose-50 px-3 py-2 rounded-lg">{error}</p>}

          <button
            type="submit"
            disabled={loading}
            className="mt-2 bg-teal-600 hover:bg-teal-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-lg transition-colors"
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>

          {mode === 'student' && (
            <p className="text-xs text-gray-400 text-center mt-1">
              Your feedback is completely anonymous and can be submitted only once per subject.
            </p>
          )}
        </form>
      </div>
    </div>
  )
}
