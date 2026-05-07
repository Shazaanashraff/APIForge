import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-gray-950 text-white flex flex-col items-center justify-center gap-4">
      <p className="text-6xl font-bold text-gray-700">404</p>
      <p className="text-gray-400">Page not found</p>
      <Link to="/" className="text-brand-400 text-sm hover:underline">
        Back to dashboard
      </Link>
    </div>
  )
}
