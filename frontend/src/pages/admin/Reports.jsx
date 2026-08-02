import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

export default function Reports() {
  const [classes, setClasses] = useState([])
  const [selectedClassId, setSelectedClassId] = useState('')
  const [generating, setGenerating] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    api.get('/admin/classes').then((res) => {
      setClasses(res.data)
      if (res.data.length > 0) setSelectedClassId(String(res.data[0].id))
    })
  }, [])

  async function handleDownload(format) {
    setError('')
    setMessage('')
    if (!selectedClassId) return
    setGenerating(true)
    try {
      const res = await api.post(
        `/reports/classes/${selectedClassId}/generate/${format}/download`,
        {},
        { responseType: 'blob' }
      )
      const blob = new Blob([res.data])
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      const className = classes.find((c) => String(c.id) === selectedClassId)?.classLabel || 'report'
      link.download = `Feedback_Report_${className.replace(/\s+/g, '_')}.${format}`
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
      setMessage(`${format.toUpperCase()} report generated and downloaded.`)
    } catch (err) {
      setError('Report generation failed. Ensure subjects and feedback responses exist for this class, and that the server has LibreOffice installed for PDF export.')
    } finally {
      setGenerating(false)
    }
  }

  return (
    <AdminLayout title="Reports">
      <div className="max-w-xl">
        <div className="mb-6">
          <label className="text-sm font-medium text-gray-600 mr-2">Class</label>
          <select value={selectedClassId} onChange={(e) => setSelectedClassId(e.target.value)}
            className="border border-gray-300 rounded-lg px-4 py-2 text-sm">
            {classes.map((c) => <option key={c.id} value={c.id}>{c.classLabel}</option>)}
          </select>
        </div>

        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          <h3 className="font-semibold text-navy-700 mb-2">Generate Feedback Report</h3>
          <p className="text-sm text-gray-500 mb-5">
            Produces the "Students' Feedback on Course Delivery" report — Theory subjects on page 1,
            Lab subjects on page 2 — with per-question averages and totals calculated automatically
            from all submitted responses. Ready to print and submit directly.
          </p>

          <div className="flex gap-3">
            <button
              disabled={generating}
              onClick={() => handleDownload('docx')}
              className="bg-navy-700 hover:bg-navy-600 disabled:opacity-60 text-white font-medium rounded-lg px-5 py-2.5 text-sm"
            >
              {generating ? 'Generating...' : 'Download DOCX'}
            </button>
            <button
              disabled={generating}
              onClick={() => handleDownload('pdf')}
              className="bg-teal-600 hover:bg-teal-700 disabled:opacity-60 text-white font-medium rounded-lg px-5 py-2.5 text-sm"
            >
              {generating ? 'Generating...' : 'Download PDF'}
            </button>
          </div>

          {message && <p className="text-sm text-teal-700 bg-teal-50 px-4 py-2.5 rounded-lg mt-4">{message}</p>}
          {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-2.5 rounded-lg mt-4">{error}</p>}
        </div>
      </div>
    </AdminLayout>
  )
}
