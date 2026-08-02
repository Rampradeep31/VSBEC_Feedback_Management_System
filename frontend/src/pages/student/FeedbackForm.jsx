import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../../api/axios'

export default function FeedbackForm() {
  const { subjectId } = useParams()
  const navigate = useNavigate()
  const [questions, setQuestions] = useState([])
  const [ratings, setRatings] = useState({})
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    load()
  }, [subjectId])

  async function load() {
    setLoading(true)
    setError('')
    try {
      const res = await api.get(`/student/subjects/${subjectId}/questions`)
      setQuestions(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load questions')
    } finally {
      setLoading(false)
    }
  }

  function setRating(questionId, value) {
    setRatings((prev) => ({ ...prev, [questionId]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    if (Object.keys(ratings).length !== questions.length) {
      setError('Please rate every question before submitting.')
      return
    }

    setSubmitting(true)
    try {
      await api.post('/student/feedback', {
        subjectId: Number(subjectId),
        answers: questions.map((q) => ({ questionId: q.id, rating: ratings[q.id] })),
      })
      setSuccess(true)
      setTimeout(() => navigate('/student/feedback'), 1500)
    } catch (err) {
      setError(err.response?.data?.message || 'Submission failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  const ratingLabels = ['Poor', 'Fair', 'Good', 'Very Good', 'Excellent']

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-navy-700 text-white px-6 py-4 shadow-md">
        <p className="text-xs uppercase tracking-wider text-teal-100/80">V.S.B. Engineering College, Karur</p>
        <h1 className="text-lg font-semibold">Anonymous Course Feedback</h1>
      </header>

      <main className="max-w-2xl mx-auto p-6">
        <button onClick={() => navigate('/student/feedback')} className="text-sm text-navy-600 mb-4 hover:underline">
          ← Back to subjects
        </button>

        {loading ? (
          <p className="text-gray-500 text-center mt-10">Loading questions...</p>
        ) : success ? (
          <div className="bg-teal-50 border border-teal-200 rounded-xl p-6 text-center">
            <p className="text-teal-700 font-semibold text-lg">Feedback submitted. Thank you!</p>
            <p className="text-sm text-teal-600 mt-1">Redirecting...</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="bg-amber-50 border border-amber-200 text-amber-800 text-sm rounded-lg px-4 py-3">
              Your response is anonymous and cannot be traced back to you. You can submit this form only once.
            </div>

            {error && <p className="text-sm text-rose-600 bg-rose-50 px-4 py-3 rounded-lg">{error}</p>}

            {questions.map((q, idx) => (
              <div key={q.id} className="bg-white border border-gray-200 rounded-xl p-5">
                <p className="font-medium text-navy-700 mb-3">
                  <span className="text-teal-600 font-semibold">Q{idx + 1}.</span> {q.questionText}
                </p>
                <div className="flex gap-2 flex-wrap">
                  {[1, 2, 3, 4, 5].map((val) => (
                    <button
                      type="button"
                      key={val}
                      onClick={() => setRating(q.id, val)}
                      className={`flex-1 min-w-[70px] py-2 rounded-lg text-sm font-medium border transition-colors ${
                        ratings[q.id] === val
                          ? 'bg-teal-600 border-teal-600 text-white'
                          : 'bg-white border-gray-200 text-gray-600 hover:border-teal-400'
                      }`}
                    >
                      {val} · {ratingLabels[val - 1]}
                    </button>
                  ))}
                </div>
              </div>
            ))}

            <button
              type="submit"
              disabled={submitting}
              className="mt-2 bg-navy-700 hover:bg-navy-600 disabled:opacity-60 text-white font-semibold py-3 rounded-lg transition-colors"
            >
              {submitting ? 'Submitting...' : 'Submit Feedback'}
            </button>
          </form>
        )}
      </main>
    </div>
  )
}
