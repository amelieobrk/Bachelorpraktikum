import React from 'react';
import {Form} from 'react-bootstrap';
import {MultipleChoiceQuestion} from "../../api/question";

interface MultipleQuestionProps {
  question: MultipleChoiceQuestion
  onSelectAnswer: (localId: number) => void
  selectedAnswers: number[]
  showSolution: boolean
  disabled: boolean
  crossedAnswers: number[]
  onCrossAnswer: (localId: number) => void
}

/**
 * Modal to display a multiple choice question.
 */
export default function MultipleChoiceQuestionDisplay(props: MultipleQuestionProps) {

  const {question, onSelectAnswer, selectedAnswers, showSolution, disabled, crossedAnswers, onCrossAnswer} = props;

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
                type="checkbox"
                name={`answer.${answer.localId}`}
                checked={selectedAnswers.includes(answer.localId)}
                onChange={() =>onSelectAnswer(answer.localId)}
                disabled={disabled || crossedAnswers.includes(answer.localId)}
                className={
                  showSolution
                    ? (answer.isCorrect === selectedAnswers.includes(answer.localId) ? 'text-success' : 'text-danger')
                    : ''
                }
                isInvalid={showSolution && answer.isCorrect !== selectedAnswers.includes(answer.localId)}
                isValid={showSolution && answer.isCorrect === selectedAnswers.includes(answer.localId)}
              />
              <Form.Check.Label
                style={crossedAnswers.includes(answer.localId) ? {textDecoration: 'line-through'} : {}}
              >
                {answer.text}
              </Form.Check.Label>
              {
                showSolution && answer.isCorrect !== selectedAnswers.includes(answer.localId) && (
                  answer.isCorrect ? (
                    <Form.Control.Feedback type="invalid">Diese Antwort wäre richtig gewesen</Form.Control.Feedback>
                  ) : (
                    <Form.Control.Feedback type="invalid">Diese Antwort ist falsch</Form.Control.Feedback>
                  )
                )
              }
            </Form.Check>
          </div>
        ))
      }
      <p>
        Punkte: {question.points}
      </p>
    </>
  )

}