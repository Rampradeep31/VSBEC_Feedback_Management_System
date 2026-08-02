import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

const YEARS = ['I', 'II', 'III', 'IV']
const SEMS = ['ODD', 'EVEN']

export default function Classes() {
  const [classes, setClasses] = useState([])
  const [academicYears, setAcademicYears] = useState([])
  const [departments, setDepartments] = useState([])
  const [form, setForm] = useState({
    academicYearId: '', departmentId: '', yearOfStudy: 'III', section: '', semester: 'ODD', classLabel: ''
  })
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    const [cRes, ayRes, dRes] = await Promise.all([
      api.get('/admin/classes'),
      api.get('/admin/academic-years'),
      api.get('/admin/departments'),
    ])
    setClasses(cRes.data)
    setAcademicYears(ayRes.data)
    setDepartments(dRes.data)
  }

  function updateForm(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    try {
      await api.post('/admin/classes', {
        ...form,
        academicYearId: Number(form.academicYearId),
        departmentId: Number(form.departmentId),
      })
      setForm({ academicYearId: '', departmentId: '', yearOfStudy: 'III', section: '', semester: 'ODD', classLabel: '' })
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add class')
    }
  }

  async function handleDelete(id) {
    if (!confirm('Delete this class? This will remove its subjects and students too.')) return
    await api.delete(`/admin/classes/${id}`)
    load()
  }

  return (
    <AdminLayout title="Classes">
      <form onSubmit={handleAdd} className="bg-white rounded-xl border border-gray-100 shadow-sm p-5 mb-6 grid grid-cols-2 md:grid-cols-3 gap-3">
        <select required value={form.academicYearId} onChange={(e) => updateForm('academicYearId', e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          <option value="">Academic Year</option>
          {academicYears.map((y) => <option key={y.id} value={y.id}>{y.yearLabel}</option>)}
        </select>

        <select required value={form.departmentId} onChange={(e) => updateForm('departmentId', e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          <option value="">Department</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.shortCode}</option>)}
        </select>

        <select value={form.yearOfStudy} onChange={(e) => updateForm('yearOfStudy', e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          {YEARS.map((y) => <option key={y} value={y}>{y} Year</option>)}
        </select>

        <input required placeholder="Section (e.g. C)" value={form.section} onChange={(e) => updateForm('section', e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />

        <select value={form.semester} onChange={(e) => updateForm('semester', e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          {SEMS.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>

        <input required placeholder="Class Label (e.g. III AI&DS C)" value={form.classLabel} onChange={(e) => updateForm('classLabel', e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm md:col-span-2" />

        <button className="bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg px-4 py-2 text-sm">Add Class</button>
      </form>

      {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

      <div className="bg-white rounded-xl border border-gray-100 shadow-sm divide-y divide-gray-100">
        {classes.map((c) => (
          <div key={c.id} className="flex items-center justify-between px-5 py-3">
            <div>
              <p className="font-medium text-navy-700">{c.classLabel}</p>
              <p className="text-xs text-gray-400">
                {c.department?.shortCode} · {c.semester} · {c.academicYear?.yearLabel} ·{' '}
                {c.feedbackOpen ? <span className="text-teal-600">Feedback Open</span> : <span className="text-gray-400">Feedback Closed</span>}
              </p>
            </div>
            <button onClick={() => handleDelete(c.id)} className="text-sm text-rose-500 hover:underline">Delete</button>
          </div>
        ))}
        {classes.length === 0 && <p className="px-5 py-4 text-sm text-gray-400">No classes yet.</p>}
      </div>
    </AdminLayout>
  )
}
