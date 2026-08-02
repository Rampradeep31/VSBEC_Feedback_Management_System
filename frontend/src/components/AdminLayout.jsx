import React from 'react'
import Navbar from './Navbar'
import Sidebar from './Sidebar'

export default function AdminLayout({ title, children }) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar title={title} />
      <div className="flex">
        <Sidebar />
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  )
}
