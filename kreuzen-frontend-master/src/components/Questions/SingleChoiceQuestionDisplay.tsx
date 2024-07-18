import React from 'react';
import {Form} from 'react-bootstrap';
import {SingleChoiceQuestion} from "../../api/question";


interface MultipleQuestionProps {
  question: SingleChoiceQuestion
  onSelectAnswer: (localId: number) => void
  selectedAnswer: number
  showSolution: boolean
  disabled: boolean
  crossedAnswers: number[]
  onCrossAnswer: (localId: number) => void
}

/**
 * Modal to display a single choice question.
 */
export default function SingleChoiceQuestionDisplay(props: MultipleQuestionProps) {

  const {question, onSelectAnswer, selectedAnswer, showSolution, disabled, crossedAnswers, onCrossAnswer} = props;

  const getFeedback = (localId : number) : string | null => {
    if (selectedAnswer === question.correctAnswerLocalId || !showSolution) {
      return null;
    }
    if (localId === question.correctAnswerLocalId) {
      return "Das wäre die richtige Antwort gewesen"
    } else {
      return null;
    }
  }

  return (
    <>
      <pre style={{font: 'inherit', whiteSpace: 'pre-wrap'}}>
        {question.text}
      </pre>
      {
        question.additionalInformation != null && (
          <pre style={{marginTop: 16, font: 'inherit', whiteSpace: 'pre-wrap'}}>
            {question.additionalInformation}
          </pre>
        )
      }
      {
        question.answers.map((answer) => (
          <div key={answer.id} style={{marginBottom: 8, display: 'flex', alignItems: "center"}}>
            <div style={{width: 32}}>
              <i
                className={`fas fa-times ${crossedAnswers.includes(answer.localId) ? 'text-danger' : 'text-secondary'}`}
                style={{marginRight: 8, cursor: 'pointer'}}
                onClick={() => onCrossAnswer(answer.localId)}
              />
            </div>
            <Form.Check style={{flex: 1}}>
              <Form.Check.Input
                type="radio"
                name={`answer.${answer.localId}`}
                checked={selectedAnswer === answer.localId}
                onChange={() => onSelectAnswer(answer.localId)}
                disabled={disabled || crossedAnswers.includes(answer.localId)}
                className={
                  (showSolution && selectedAnswer === answer.localId)
                    ? (selectedAnswer === question.correctAnswerLocalId ? 'text-success' : 'text-danger')
                    : ''
                }
                isInvalid={showSolution && selectedAnswer === answer.localId && selectedAnswer !== question.correctAnswerLocalId}
                isValid={showSolution && answer.localId === question.correctAnswerLocalId}
              />
              <Form.Check.Label
                style={crossedAnswers.includes(answer.localId) ? {textDecoration: 'line-through'} : {}}
              >
                {answer.text}
              </Form.Check.Label>
              {
                getFeedback(answer.localId) && (
                  <Form.Control.Feedback type="invalid">{getFeedback(answer.localId)}</Form.Control.Feedback>
                )
              }
            </Form.Check>

          </div>
        ))
      }
      <p>
        Punkte: {question.points}
      </p>
      {
        showSolution && (
          <p>
            Richtige Antwort: {question.answers.find(a => a.localId === question.correctAnswerLocalId)?.text}
          </p>
        )
      }
    </>
  )

}