import React, {useEffect, useState} from 'react';
import {Button, Card} from 'react-bootstrap';
import {MultipleChoiceQuestion, Question, SingleChoiceQuestion} from "../../../api/question";
import SingleChoiceQuestionReview from "../../../components/Sessions/Review/SingleChoiceQuestionReview";
import MultipleChoiceQuestionReview from '../../../components/Sessions/Review/MultipleChoiceQuestionReview';
import {Link, useHistory} from "react-router-dom";
import api from "../../../api";
import CardHeader from "../../../components/General/CardHeader";

/**
 * Page to review the answer to a question.
 */
export default function SessionReview(props: {sessionId: number, questionId: number}) {

  const {sessionId, questionId} = props;

  const history = useHistory();

  const [question, setQuestion] = useState<Question | null>(null);
  const [totalQuestionCount, setTotalQuestionCount] = useState(-1);

  useEffect(() => {
    api.session.getLocalQuestion(sessionId, questionId).then(setQuestion)
  }, [sessionId, questionId])
  useEffect(() => {
    api.session.getQuestionCountOfSession(sessionId).then(setTotalQuestionCount);
  }, [sessionId])
  useEffect(() => {
    if (questionId > totalQuestionCount && totalQuestionCount !== -1) {
      history.push(`/user/session/${sessionId}/review/${totalQuestionCount}`)
    }
  }, [history, sessionId, questionId, totalQuestionCount])

  const renderQuestionType = () => {
    if (!question) {
      return null;
    }
    switch (question.type.toLowerCase()) {
      case 'single-choice':
        return (
          <SingleChoiceQuestionReview question={question as SingleChoiceQuestion} sessionId={sessionId} localQuestionId={questionId} />
        )
      case 'multiple-choice':
        return (
          <MultipleChoiceQuestionReview question={question as MultipleChoiceQuestion} sessionId={sessionId} localQuestionId={questionId} />
        )
      default:
        return "Fragentyp kann nicht angezeigt werden."
    }
  }

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Session Review" secondary />
        {
          question ? (
            <>
              {
                renderQuestionType()
              }
              <div style={{marginTop: 32, display: 'flex', justifyContent: 'space-between'}}>
                <div>
                  {
                    questionId > 1 && (
                      <Button as={Link} to={`/user/sessions/${sessionId}/review/${questionId - 1}`} style={{marginRight: 8}}>
                        Zurück
                      </Button>
                    )
                  }
                  {
                    questionId < totalQuestionCount && (
                      <Button as={Link} to={`/user/sessions/${sessionId}/review/${questionId + 1}`}>
                        Weiter
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
