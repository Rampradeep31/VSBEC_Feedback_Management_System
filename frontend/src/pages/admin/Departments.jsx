import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

export default function Departments() {
  const [departments, setDepartments] = useState([])
  const [name, setName] = useState('')
  const [shortCode, setShortCode] = useState('')
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    const res = await api.get('/admin/departments')
    setDepartments(res.data)
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    try {
      await api.post('/admin/departments', { name, shortCode })
      setName(''); setShortCode('')
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add department')
    }
  }

  async function handleDelete(id) {
    if (!confirm('Delete this department?')) return
    await api.delete(`/admin/departments/${id}`)
    load()
  }

  return (
    <AdminLayout title="Departments">
      <div className="max-w-xl">
        <form onSubmit={handleAdd} className="flex gap-2 mb-6">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. ARTIFICIAL INTELLIGENCE AND DATA SCIENCE"
            required
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <input
            value={shortCode}
            onChange={(e) => setShortCode(e.target.value)}
            placeholder="AIDS"
            required
            className="w-28 border border-gray-300 rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <button className="bg-teal-600 hover:bg-teal-700 text-white font-medium px-5 rounded-lg">Add</button>
        </form>

        {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

        <div className="bg-white rounded-xl border border-gray-100 shadow-sm divide-y divide-gray-100">
          {departments.map((d) => (
            <div key={d.id} className="flex items-center justify-between px-5 py-3">
              <div>
                <p className="font-medium text-navy-700">{d.name}</p>
                <p className="text-xs text-gray-400">{d.shortCode}</p>
              </div>
              <button onClick={() => handleDelete(d.id)} className="text-sm text-rose-500 hover:underline">Delete</button>
            </div>
          ))}
          {departments.length === 0 && <p className="px-5 py-4 text-sm text-gray-400">No departments yet.</p>}
        </div>
      </div>
    </AdminLayout>
  )
}
