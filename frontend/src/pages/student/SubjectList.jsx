import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../../api/axios'
import { useAuth } from '../../context/AuthContext'

export default function SubjectList() {
  const [subjects, setSubjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const { user, logout } = useAuth()

  useEffect(() => {
    load()
  }, [])

  async function load() {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/student/subjects')
      setSubjects(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load subjects')
    } finally {
      setLoading(false)
    }
  }

  const pending = subjects.filter((s) => !s.submitted)
  const done = subjects.filter((s) => s.submitted)

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-navy-700 text-white px-6 py-4 flex items-center justify-between shadow-md">
        <div>
          <p className="text-xs uppercase tracking-wider text-teal-100/80">V.S.B. Engineering College, Karur</p>
          <h1 className="text-lg font-semibold">Course Feedback — {user?.classLabel}</h1>
        </div>
        <button onClick={() => { logout(); navigate('/login') }} className="text-sm bg-navy-600 hover:bg-navy-500 px-3 py-1.5 rounded-md">
          Logout
        </button>
      </header>

      <main className="max-w-3xl mx-auto p-6">
        {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

        {loading ? (
          <p className="text-gray-500 text-center mt-10">Loading subjects...</p>
        ) : subjects.length === 0 ? (
          <p className="text-gray-500 text-center mt-10">No subjects found for your class, or feedback is not yet configured.</p>
        ) : (
          <>
            {pending.length > 0 && (
              <section className="mb-8">
                <h2 className="text-sm font-semibold text-gray-500 uppercase mb-3">Pending ({pending.length})</h2>
                <div className="flex flex-col gap-3">
                  {pending.map((s) => (
                    <button
                      key={s.subjectId}
                      onClick={() => navigate(`/student/feedback/${s.subjectId}`)}
                      className="bg-white border border-gray-200 hover:border-teal-400 hover:shadow-md transition-all rounded-xl p-4 text-left flex items-center justify-between"
                    >
                      <div>
                        <p className="font-semibold text-navy-700">{s.subjectName}</p>
                        <p className="text-sm text-gray-500">{s.facultyName} · {s.subjectType === 'LAB' ? 'Lab' : 'Theory'}</p>
                      </div>
                      <span className="text-teal-600 text-sm font-medium">Give Feedback →</span>
                    </button>
                  ))}
                </div>
              </section>
            )}

            {done.length > 0 && (
              <section>
                <h2 className="text-sm font-semibold text-gray-500 uppercase mb-3">Submitted ({done.length})</h2>
                <div className="flex flex-col gap-2">
                  {done.map((s) => (
                    <div key={s.subjectId} className="bg-gray-100 rounded-xl p-4 flex items-center justify-between opacity-70">
                      <div>
                        <p className="font-medium text-gray-600">{s.subjectName}</p>
                        <p className="text-sm text-gray-400">{s.facultyName} · {s.subjectType === 'LAB' ? 'Lab' : 'Theory'}</p>
                      </div>
                      <span className="text-xs font-semibold text-teal-700 bg-teal-50 px-2 py-1 rounded-full">✓ Submitted</span>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {pending.length === 0 && done.length > 0 && (
              <p className="text-center text-teal-700 font-medium mt-6">
                You've submitted feedback for every subject. Thank you!
              </p>
            )}
          </>
        )}
      </main>
    </div>
  )
}
