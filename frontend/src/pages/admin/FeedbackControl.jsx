import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

export default function FeedbackControl() {
  const [classes, setClasses] = useState([])
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    const res = await api.get('/admin/classes')
    setClasses(res.data)
  }

  async function toggle(classId, open) {
    setError('')
    try {
      await api.patch(`/admin/classes/${classId}/feedback-window`, { open })
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not update feedback window')
    }
  }

  return (
    <AdminLayout title="Feedback Control">
      {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

      <div className="bg-white rounded-xl border border-gray-100 shadow-sm divide-y divide-gray-100">
        {classes.map((c) => (
          <div key={c.id} className="flex items-center justify-between px-5 py-4">
            <div>
              <p className="font-medium text-navy-700">{c.classLabel}</p>
              <p className="text-xs text-gray-400">
                {c.feedbackOpen ? 'Students can currently submit feedback' : 'Feedback submission is closed'}
              </p>
            </div>
            <button
              onClick={() => toggle(c.id, !c.feedbackOpen)}
              className={`text-sm font-semibold px-4 py-2 rounded-lg transition-colors ${
                c.feedbackOpen
                  ? 'bg-rose-50 text-rose-600 hover:bg-rose-100'
                  : 'bg-teal-600 text-white hover:bg-teal-700'
              }`}
            >
              {c.feedbackOpen ? 'Close Feedback' : 'Open Feedback'}
            </button>
          </div>
        ))}
        {classes.length === 0 && <p className="px-5 py-4 text-sm text-gray-400">No classes configured yet.</p>}
      </div>
    </AdminLayout>
  )
}
