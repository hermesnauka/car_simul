import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Navbar() {
  const { isAuthenticated, username, logout } = useAuth()
  const navigate = useNavigate()

  if (!isAuthenticated) return null

  return (
    <nav className="navbar">
      <span className="navbar-brand">🚗 Driving Simulator</span>
      <div className="navbar-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/exam">Exam Mode</Link>
      </div>
      <div className="navbar-user">
        <span>{username}</span>
        <button
          className="btn btn-small"
          onClick={() => {
            logout()
            navigate('/login')
          }}
        >
          Log out
        </button>
      </div>
    </nav>
  )
}
