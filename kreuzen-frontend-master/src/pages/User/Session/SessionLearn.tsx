import React, { useEffect, useState } from 'react';
import { Button, Card } from 'react-bootstrap';
import { Link, useHistory } from "react-router-dom";
import { MultipleChoiceQuestion, Question, SingleChoiceQuestion } from "../../../api/question";
import api from "../../../api";
import { Session } from "../../../api/session";
import LearnSingleChoiceQuestion from "../../../components/Sessions/Learn/LearnSingleChoiceQuestion";
import LearnMultipleChoiceQuestion from "../../../components/Sessions/Learn/LearnMultipleChoiceQuestion";

/**
 * Page to lern a session. It displays one question at a time
 */
export default function SessionLearn(props: { sessionId: number, questionId: number }) {

  const { sessionId, questionId } = props;

  const history = useHistory();

  const [question, setQuestion] = useState<Question | null>(null);
  const [session, setSession] = useState<Session | null>(null);
  const [totalQuestionCount, setTotalQuestionCount] = useState(-1);
  const [loadedLocalId, setLoadedLocalId] = useState<number | null>(null);

  const [timer, setTimer] = useState(0);
  const [isFinished, setIsFinished] = useState(false);

  useEffect(() => {
    setTimer(0);
    setIsFinished(false);
    setLoadedLocalId(null);
    api.session.getLocalQuestion(sessionId, questionId).then(setQuestion)
    api.session.getLocalQuestionStatus(sessionId, questionId).then((status) => {
      setTimer(status.time)
      setIsFinished(status.isSubmitted)
      setLoadedLocalId(questionId);
    })
  }, [sessionId, questionId])
  useEffect(() => {
    api.session.getQuestionCountOfSession(sessionId).then(setTotalQuestionCount);
    api.session.getSession(sessionId).then(setSession);
  }, [sessionId])
  useEffect(() => {
    if (questionId > totalQuestionCount && totalQuestionCount !== -1) {
      history.push(`/user/session/${sessionId}/review/${totalQuestionCount}`)
    }
  }, [history, sessionId, questionId, totalQuestionCount])

  useEffect(() => {
    const timerInterval: number = setInterval(() => {
      if (loadedLocalId !== null && !isFinished) {
        setTimer(x => x + 1)
      }
    }, 1000);
    return () => clearInterval(timerInterval);
  })
  useEffect(() => {
    if (!isFinished && loadedLocalId === questionId) {
      api.session.setTime(sessionId, questionId, timer).then(() => { })
    }
  }, [sessionId, questionId, timer, loadedLocalId, isFinished]);

  const handleFinishQuestion = () => {
    setIsFinished(true);
    api.session.finishQuestion(sessionId, questionId).then(() => { });
  }

  const handleFinishSession = () => {
    setIsFinished(true);
    api.session.finishQuestion(sessionId, questionId).then(() => {
      api.session.finishSession(sessionId)
        .then(() => history.push(`/user/sessions/${sessionId}`))
    });
  }

  const renderQuestionType = () => {
    if (!question) {
      return null;
    }
    switch (question.type.toLowerCase()) {
      case 'single-choice':
        return (
          <LearnSingleChoiceQuestion
            question={question as SingleChoiceQuestion}
            sessionId={sessionId}
            localQuestionId={questionId}
            sessionType={session?.type || 'practice'}
            isFinished={isFinished}
          />
        )
      case 'multiple-choice':
        return (
          <LearnMultipleChoiceQuestion
            question={question as MultipleChoiceQuestion}
            sessionId={sessionId}
            localQuestionId={questionId}
            sessionType={session?.type || 'practice'}
            isFinished={isFinished}
          />
        )
      default:
        return "Fragentyp kann nicht angezeigt werden."
    }
  }

  return (
    <Card>
      <Card.Body>
        <Card.Title>
          <h2>Session Durchführen</h2>
          <span className="float-right">
            {timer}s
          </span>
        </Card.Title>
        {
          (question && session && loadedLocalId) ? (
            <>
              {
                renderQuestionType()
              }
              <div style={{ marginTop: 32, display: 'flex', justifyContent: 'space-between' }}>
                <div>
                  {
                    questionId > 1 && (
                      <Button as={Link} to={`/user/sessions/${sessionId}/learn/${questionId - 1}`} style={{ marginRight: 8 }}>
                        Zurück
                      </Button>
                    )
                  }
                  {
                    !isFinished && (
                      <Button variant="outline-success" style={{ marginRight: 8 }} onClick={handleFinishQuestion}>
                        Frage abgeben
                      </Button>
                    )
                  }
                  {
                    questionId < totalQuestionCount ? (
                      <Button as={Link} to={`/user/sessions/${sessionId}/learn/${questionId + 1}`}>
                        Weiter
                      </Button>
                    ) : (
                      <Button variant="success" onClick={handleFinishSession}>
                        Auswerten
                      </Button>
                    )
                  }
                </div>
                <div>
                  <Button as={Link} to={`/user/sessions/${sessionId}`} variant="secondary">
                    Übersicht
                  </Button>
                </div>
              </div>
            </>
          ) : (
            <>
              Loading...
            </>
          )
        }
      </Card.Body>
    </Card>
  );
}
