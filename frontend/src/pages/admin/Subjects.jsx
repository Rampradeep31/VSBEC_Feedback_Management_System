import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

export default function Subjects() {
  const [classes, setClasses] = useState([])
  const [selectedClassId, setSelectedClassId] = useState('')
  const [subjects, setSubjects] = useState([])
  const [faculty, setFaculty] = useState([])
  const [form, setForm] = useState({ facultyId: '', subjectName: '', subjectCode: '', subjectType: 'THEORY', displayOrder: 0 })
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/admin/classes').then((res) => {
      setClasses(res.data)
      if (res.data.length > 0) setSelectedClassId(String(res.data[0].id))
    })
    api.get('/admin/faculty').then((res) => setFaculty(res.data))
  }, [])

  useEffect(() => {
    if (selectedClassId) loadSubjects(selectedClassId)
  }, [selectedClassId])

  async function loadSubjects(classId) {
    const res = await api.get(`/admin/classes/${classId}/subjects`)
    setSubjects(res.data)
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    try {
      await api.post('/admin/subjects', {
        ...form,
        classId: Number(selectedClassId),
        facultyId: Number(form.facultyId),
        displayOrder: Number(form.displayOrder) || 0,
      })
      setForm({ facultyId: '', subjectName: '', subjectCode: '', subjectType: 'THEORY', displayOrder: 0 })
      loadSubjects(selectedClassId)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add subject')
    }
  }

  async function handleDelete(id) {
    if (!confirm('Delete this subject? Its feedback question set stays, but responses for it will remain orphaned.')) return
    await api.delete(`/admin/subjects/${id}`)
    loadSubjects(selectedClassId)
  }

  return (
    <AdminLayout title="Subjects">
      <div className="mb-6">
        <label className="text-sm font-medium text-gray-600 mr-2">Class</label>
        <select value={selectedClassId} onChange={(e) => setSelectedClassId(e.target.value)}
          className="border border-gray-300 rounded-lg px-4 py-2 text-sm">
          {classes.map((c) => <option key={c.id} value={c.id}>{c.classLabel}</option>)}
        </select>
      </div>

      <form onSubmit={handleAdd} className="bg-white rounded-xl border border-gray-100 shadow-sm p-5 mb-6 grid grid-cols-1 md:grid-cols-5 gap-3">
        <input required placeholder="Subject name (e.g. Cloud Service Management)" value={form.subjectName}
          onChange={(e) => setForm({ ...form, subjectName: e.target.value })}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm md:col-span-2" />
        <input placeholder="Subject code" value={form.subjectCode}
          onChange={(e) => setForm({ ...form, subjectCode: e.target.value })}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
        <select required value={form.facultyId} onChange={(e) => setForm({ ...form, facultyId: e.target.value })}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          <option value="">Faculty</option>
          {faculty.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
        </select>
        <select value={form.subjectType} onChange={(e) => setForm({ ...form, subjectType: e.target.value })}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
          <option value="THEORY">Theory</option>
          <option value="LAB">Lab</option>
        </select>
        <button className="bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg px-4 py-2 text-sm md:col-span-5">Add Subject</button>
      </form>

      <p className="text-xs text-gray-400 mb-3">
        Tip: create "X" as Theory and "X Lab" as a separate Lab subject — each gets its own 10-question set and its own row in the printed report, just like the source report.
      </p>

      {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mb-4">{error}</p>}

      <div className="bg-white rounded-xl border border-gray-100 shadow-sm divide-y divide-gray-100">
        {subjects.map((s) => (
          <div key={s.id} className="flex items-center justify-between px-5 py-3">
            <div>
              <p className="font-medium text-navy-700">{s.subjectName}</p>
              <p className="text-xs text-gray-400">{s.faculty?.name} · {s.subjectType}</p>
            </div>
            <button onClick={() => handleDelete(s.id)} className="text-sm text-rose-500 hover:underline">Delete</button>
          </div>
        ))}
        {subjects.length === 0 && <p className="px-5 py-4 text-sm text-gray-400">No subjects for this class yet.</p>}
      </div>
    </AdminLayout>
  )
}
