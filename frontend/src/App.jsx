import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'

import Login from './pages/Login'
import SubjectList from './pages/student/SubjectList'
import FeedbackForm from './pages/student/FeedbackForm'

import Dashboard from './pages/admin/Dashboard'
import AcademicYears from './pages/admin/AcademicYears'
import Departments from './pages/admin/Departments'
import Classes from './pages/admin/Classes'
import FacultyPage from './pages/admin/Faculty'
import Subjects from './pages/admin/Subjects'
import Students from './pages/admin/Students'
import FeedbackControl from './pages/admin/FeedbackControl'
import Reports from './pages/admin/Reports'
import ChangePassword from './pages/admin/ChangePassword'


export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      {/* Student */}
      <Route path="/student/feedback" element={
        <ProtectedRoute requiredRole="STUDENT"><SubjectList /></ProtectedRoute>
      } />
      <Route path="/student/feedback/:subjectId" element={
        <ProtectedRoute requiredRole="STUDENT"><FeedbackForm /></ProtectedRoute>
      } />

      {/* Admin */}
      <Route path="/admin/dashboard" element={
        <ProtectedRoute requiredRole="ADMIN"><Dashboard /></ProtectedRoute>
      } />
      <Route path="/admin/academic-years" element={
        <ProtectedRoute requiredRole="ADMIN"><AcademicYears /></ProtectedRoute>
      } />
      <Route path="/admin/departments" element={
        <ProtectedRoute requiredRole="ADMIN"><Departments /></ProtectedRoute>
      } />
      <Route path="/admin/classes" element={
        <ProtectedRoute requiredRole="ADMIN"><Classes /></ProtectedRoute>
      } />
      <Route path="/admin/faculty" element={
        <ProtectedRoute requiredRole="ADMIN"><FacultyPage /></ProtectedRoute>
      } />
      <Route path="/admin/subjects" element={
        <ProtectedRoute requiredRole="ADMIN"><Subjects /></ProtectedRoute>
      } />
      <Route path="/admin/students" element={
        <ProtectedRoute requiredRole="ADMIN"><Students /></ProtectedRoute>
      } />
      <Route path="/admin/feedback-control" element={
        <ProtectedRoute requiredRole="ADMIN"><FeedbackControl /></ProtectedRoute>
      } />
      <Route path="/admin/reports" element={
        <ProtectedRoute requiredRole="ADMIN"><Reports /></ProtectedRoute>
      } />
      <Route path="/admin/change-password" element={
        <ProtectedRoute requiredRole="ADMIN"><ChangePassword /></ProtectedRoute>
      } />


      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
