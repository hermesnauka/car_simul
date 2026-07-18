import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { finishExam } from '../api/examApi'
import type { ExamResult } from '../types'

/**
 * Renders the verdict the backend computed. This page receives the result; it
 * has no logic that could produce one.
 */
export default function ResultsPage() {
  const { sessionId } = useParams()
  const [result, setResult] = useState<ExamResult | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!sessionId) return
    finishExam(Number(sessionId))
      .then(setResult)
      .catch(() => setError('Could not load the exam result'))
  }, [sessionId])

  if (error) return <p className="error">{error}</p>
  if (!result) return <p>Grading…</p>

  const passed = result.status === 'PASSED'

  return (
    <div className="card results-card">
      <h1 className={passed ? 'verdict-pass' : 'verdict-fail'}>
        {passed ? '✅ PASSED' : '❌ FAILED'}
      </h1>
      <p className="final-score">
        Final score: <strong>{result.finalScore}</strong> / 100
      </p>
      <p>
        {result.totalInfractionCount} infraction(s), {result.criticalInfractionCount} critical
      </p>
      {result.infractions.length > 0 && (
        <table className="infractions-table">
          <thead>
            <tr>
              <th>Rule</th>
              <th>Severity</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            {result.infractions.map((inf, i) => (
              <tr key={i} className={inf.severity === 'CRITICAL' ? 'row-critical' : ''}>
                <td>{inf.ruleCode}</td>
                <td>{inf.severity}</td>
                <td>{inf.message}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <Link className="btn btn-primary" to="/exam">
        Take another exam
      </Link>
    </div>
  )
}
