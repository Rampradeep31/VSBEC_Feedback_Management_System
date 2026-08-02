import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

export default function AcademicYears() {
  const [years, setYears] = useState([])
  const [yearLabel, setYearLabel] = useState('')
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    const res = await api.get('/admin/academic-years')
    setYears(res.data)
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    try {
      await api.post('/admin/academic-years', { yearLabel })
      setYearLabel('')
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add academic year')
    }
  }

  async function handleDelete(id) {
    if (!confirm('Delete this academic year?')) return
    await api.delete(`/admin/academic-years/${id}`)
    load()
  }

  return (
    <AdminLayout title="Academic Years">
      <div className="max-w-xl">
        <form onSubmit={handleAdd} className="flex gap-2 mb-6">
          <input
            value={yearLabel}
            onChange={(e) => setYearLabel(e.target.value)}
            placeholder="e.g. 2026-2027"
            required
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <button className="bg-teal-600 hover:bg-teal-700 text-white font-medium px-5 rounded-lg">Add</button>
        </form>

        {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

        <div className="bg-white rounded-xl border border-gray-100 shadow-sm divide-y divide-gray-100">
          {years.map((y) => (
            <div key={y.id} className="flex items-center justify-between px-5 py-3">
              <span className="font-medium text-navy-700">{y.yearLabel}</span>
              <button onClick={() => handleDelete(y.id)} className="text-sm text-rose-500 hover:underline">Delete</button>
            </div>
          ))}
          {years.length === 0 && <p className="px-5 py-4 text-sm text-gray-400">No academic years yet.</p>}
        </div>
      </div>
    </AdminLayout>
  )
}
