import React, {useEffect, useState} from 'react';
import {MultipleChoiceQuestion} from "../../../api/question";
import MultipleChoiceQuestionDisplay from "../../Questions/MultipleChoiceQuestionDisplay";
import api from "../../../api";

interface LearnMultipleChoiceQuestionProps {
  question: MultipleChoiceQuestion
  sessionId: number
  localQuestionId: number
  sessionType: string
  isFinished?: boolean
}

/**
 * Modal to learn a multiple choice question.
 */
export default function LearnMultipleChoiceQuestion(props: LearnMultipleChoiceQuestionProps) {

  const {question, sessionId, localQuestionId, isFinished, sessionType} = props;

  const [selectedOptions, setSelectedOptions] = useState<number[]>([]);
  const [crossedAnswers, setCrossedAnswers] = useState<number[]>([]);

  useEffect(() => {
    api.session.getMultipleChoiceSelection(sessionId, localQuestionId).then(selection => {
      setSelectedOptions(selection.filter(s => s.isChecked).map(s => s.localAnswerId));
      setCrossedAnswers(selection.filter(s => s.isCrossed).map(s => s.localAnswerId));
    })
  }, [sessionId, localQuestionId])

  const handleCrossAnswer = (i: number) => {
    const newCrossed = crossedAnswers.includes(i)
      ? crossedAnswers.filter(x => x !== i)
      : [...crossedAnswers, i];
    setCrossedAnswers(newCrossed);
    api.session.setMultipleChoiceAnswer(sessionId, localQuestionId, selectedOptions, newCrossed).then(() => {});
  }

  const handleSelectAnswer = (i: number) => {
    const newSelected = selectedOptions.includes(i) ? selectedOptions.filter(x => x !== i) : [...selectedOptions, i]
    setSelectedOptions(newSelected);
    api.session.setMultipleChoiceAnswer(sessionId, localQuestionId, newSelected, crossedAnswers).then(() => {});
  }

  return (
    <>
      <MultipleChoiceQuestionDisplay
        question={question}
        showSolution={sessionType === 'practice' && (isFinished || false)}
        disabled={isFinished || false}
        selectedAnswers={selectedOptions}
        crossedAnswers={crossedAnswers}
        onCrossAnswer={handleCrossAnswer}
        onSelectAnswer={handleSelectAnswer}
      />
    </>
  )

}