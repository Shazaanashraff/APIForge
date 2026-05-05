// Placeholder App — full implementation in S16 (Frontend Foundation)
export default function App() {
  return (
    <div className="min-h-screen bg-gray-950 text-white flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-indigo-400 mb-2">APIForge</h1>
        <p className="text-gray-400">Frontend coming in S16. Backend is running at{' '}
          <a href="http://localhost:8081/actuator/health"
             className="text-indigo-300 underline"
             target="_blank" rel="noreferrer">
            localhost:8081
          </a>
        </p>
      </div>
    </div>
  )
}
