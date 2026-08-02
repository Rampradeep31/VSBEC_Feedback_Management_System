import React, { useEffect, useState } from 'react'
import AdminLayout from '../../components/AdminLayout'
import api from '../../api/axios'

export default function Students() {
  const [classes, setClasses] = useState([])
  const [selectedClassId, setSelectedClassId] = useState('')
  const [file, setFile] = useState(null)
  const [result, setResult] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/admin/classes').then((res) => {
      setClasses(res.data)
      if (res.data.length > 0) setSelectedClassId(String(res.data[0].id))
    })
  }, [])

  async function handleUpload(e) {
    e.preventDefault()
    setError('')
    setResult(null)
    if (!file || !selectedClassId) return

    const formData = new FormData()
    formData.append('file', file)

    setUploading(true)
    try {
      const res = await api.post(`/admin/classes/${selectedClassId}/students/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      setResult(res.data)
      setFile(null)
    } catch (err) {
      setError(err.response?.data?.message || 'Upload failed')
    } finally {
      setUploading(false)
    }
  }

  return (
    <AdminLayout title="Student Upload">
      <div className="max-w-xl">
        <div className="mb-4">
          <label className="text-sm font-medium text-gray-600 mr-2">Class</label>
          <select value={selectedClassId} onChange={(e) => setSelectedClassId(e.target.value)}
            className="border border-gray-300 rounded-lg px-4 py-2 text-sm">
            {classes.map((c) => <option key={c.id} value={c.id}>{c.classLabel}</option>)}
          </select>
        </div>

        <form onSubmit={handleUpload} className="bg-white rounded-xl border border-gray-100 shadow-sm p-5 flex flex-col gap-3">
          <p className="text-sm text-gray-500">
            Excel file needs a header row with columns <strong>RegisterNumber</strong>, <strong>Name</strong>,
            and optionally <strong>Email</strong> (column order doesn't matter).
          </p>
          <input
            type="file"
            accept=".xlsx,.xls"
            onChange={(e) => setFile(e.target.files[0])}
            className="text-sm"
          />
          <button
            disabled={!file || uploading}
            className="self-start bg-teal-600 hover:bg-teal-700 disabled:opacity-60 text-white font-medium rounded-lg px-5 py-2 text-sm"
          >
            {uploading ? 'Uploading...' : 'Upload Students'}
          </button>
        </form>

        {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg mt-4">{error}</p>}

        {result && (
          <div className="bg-teal-50 border border-teal-200 rounded-xl p-5 mt-4 text-sm text-teal-800">
            <p><strong>{result.imported}</strong> students imported.</p>
            <p><strong>{result.skippedDuplicates}</strong> duplicates skipped (already existed).</p>
            <p>Total rows processed: {result.totalRows}</p>
            {result.errors?.length > 0 && (
              <div className="mt-2">
                <p className="font-semibold">Row errors:</p>
                <ul className="list-disc list-inside">
                  {result.errors.map((e, i) => <li key={i}>{e}</li>)}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    </AdminLayout>
  )
}
