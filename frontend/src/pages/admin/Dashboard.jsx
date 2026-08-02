import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import StatCard from '../../components/StatCard'
import api from '../../api/axios'

export default function Dashboard() {
  const [classes, setClasses] = useState([])
  const [selectedClassId, setSelectedClassId] = useState('')
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/admin/classes').then((res) => {
      setClasses(res.data)
      if (res.data.length > 0) setSelectedClassId(String(res.data[0].id))
    })
  }, [])

  useEffect(() => {
    if (selectedClassId) loadDashboard(selectedClassId)
  }, [selectedClassId])

  async function loadDashboard(classId) {
    setLoading(true)
    setError('')
    try {
      const res = await api.get(`/admin/dashboard/classes/${classId}`)
      setData(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load dashboard')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AdminLayout title="Admin Dashboard">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-navy-700">Class Overview</h2>
        <select
          value={selectedClassId}
          onChange={(e) => setSelectedClassId(e.target.value)}
          className="border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
        >
          {classes.map((c) => (
            <option key={c.id} value={c.id}>{c.classLabel}</option>
          ))}
        </select>
      </div>

      {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

      {loading || !data ? (
        <p className="text-gray-500">Loading...</p>
      ) : (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            <StatCard label="Total Students" value={data.totalStudents} accent="navy" />
            <StatCard label="Feedback Submitted" value={data.feedbackSubmittedCount} accent="teal" />
            <StatCard label="Pending" value={data.pendingCount} accent="amber" />
            <StatCard label="Completion" value={data.completionPercentage} suffix="%" accent="teal" />
          </div>

          <div className={`mb-8 rounded-xl px-4 py-3 text-sm font-medium ${
            data.feedbackOpen ? 'bg-teal-50 text-teal-700' : 'bg-gray-100 text-gray-500'
          }`}>
            Feedback window is currently <strong>{data.feedbackOpen ? 'OPEN' : 'CLOSED'}</strong> for this class.
            Manage it under "Feedback Control".
          </div>

          <div className="grid md:grid-cols-2 gap-6">
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
              <h3 className="font-semibold text-navy-700 mb-3">Faculty List</h3>
              <ul className="divide-y divide-gray-100">
                {data.facultyList.map((f) => (
                  <li key={f.facultyId} className="py-2 flex justify-between text-sm">
                    <span>{f.name}</span>
                    <span className="text-gray-400">{f.subjectCount} subject(s)</span>
                  </li>
                ))}
                {data.facultyList.length === 0 && <p className="text-sm text-gray-400 py-2">No faculty assigned yet.</p>}
              </ul>
            </div>

            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
              <h3 className="font-semibold text-navy-700 mb-3">Subject List</h3>
              <ul className="divide-y divide-gray-100">
                {data.subjectList.map((s) => (
                  <li key={s.subjectId} className="py-2 flex justify-between text-sm">
                    <div>
                      <p>{s.subjectName}</p>
                      <p className="text-xs text-gray-400">{s.facultyName} · {s.subjectType}</p>
                    </div>
                    <span className="text-gray-400">{s.responseCount} response(s)</span>
                  </li>
                ))}
                {data.subjectList.length === 0 && <p className="text-sm text-gray-400 py-2">No subjects configured yet.</p>}
              </ul>
            </div>
          </div>
        </>
      )}
    </AdminLayout>
  )
}
