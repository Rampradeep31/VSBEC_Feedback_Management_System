import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

export default function FacultyPage() {
  const [faculty, setFaculty] = useState([])
  const [departments, setDepartments] = useState([])
  const [form, setForm] = useState({ name: '', email: '', departmentId: '' })
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    const [fRes, dRes] = await Promise.all([api.get('/admin/faculty'), api.get('/admin/departments')])
    setFaculty(fRes.data)
    setDepartments(dRes.data)
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    try {
      await api.post('/admin/faculty', { ...form, departmentId: Number(form.departmentId) })
      setForm({ name: '', email: '', departmentId: '' })
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add faculty')
    }
  }

  async function handleDelete(id) {
    if (!confirm('Delete this faculty member?')) return
    await api.delete(`/admin/faculty/${id}`)
    load()
  }

  return (
    <AdminLayout title="Faculty">
      <form onSubmit={handleAdd} className="bg-white rounded-xl border border-gray-100 shadow-sm p-5 mb-6 grid grid-cols-1 md:grid-cols-4 gap-3">
        <input required placeholder="Full name (e.g. Mr.R. Muthuchelvan)" value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm md:col-span-2" />
        <input type="email" placeholder="Email (optional)" value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
        <select required value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          <option value="">Department</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.shortCode}</option>)}
        </select>
        <button className="bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg px-4 py-2 text-sm md:col-span-4">Add Faculty</button>
      </form>

      {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

      <div className="bg-white rounded-xl border border-gray-100 shadow-sm divide-y divide-gray-100">
        {faculty.map((f) => (
          <div key={f.id} className="flex items-center justify-between px-5 py-3">
            <div>
              <p className="font-medium text-navy-700">{f.name}</p>
              <p className="text-xs text-gray-400">{f.department?.shortCode} {f.email ? `· ${f.email}` : ''}</p>
            </div>
            <button onClick={() => handleDelete(f.id)} className="text-sm text-rose-500 hover:underline">Delete</button>
          </div>
        ))}
        {faculty.length === 0 && <p className="px-5 py-4 text-sm text-gray-400">No faculty yet.</p>}
      </div>
    </AdminLayout>
  )
}
