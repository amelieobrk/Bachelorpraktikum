import React, {useEffect, useState} from 'react';
import SingleChoiceQuestionDisplay from '../../Questions/SingleChoiceQuestionDisplay';
import {SingleChoiceQuestion} from "../../../api/question";
import api from "../../../api";

interface LearnSingleChoiceQuestionProps {
  question: SingleChoiceQuestion
  sessionId: number
  localQuestionId: number
  sessionType: string
  isFinished?: boolean
}

/**
 * Modal to learn a single choice question.
 */
export default function LearnSingleChoiceQuestion(props: LearnSingleChoiceQuestionProps) {

  const {question, sessionId, localQuestionId, isFinished, sessionType} = props;

  const [selectedOption, setSelectedOption] = useState(-1);
  const [crossedAnswers, setCrossedAnswers] = useState<number[]>([])

  useEffect(() => {
    api.session.getSingleChoiceSelection(sessionId, localQuestionId).then(selection => {
      const so = selection.find(x => x.isChecked)?.localAnswerId || -1;
      setSelectedOption(so);
      setCrossedAnswers(selection.filter(x => x.isCrossed).map(x => x.localAnswerId))
    })
  }, [sessionId, localQuestionId])

  const handleCrossAnswer = (i: number) => {
    const newCrossed = crossedAnswers.includes(i)
      ? crossedAnswers.filter(x => x !== i)
      : [...crossedAnswers, i];
    setCrossedAnswers(newCrossed);
    api.session.setSingleChoiceAnswer(sessionId, localQuestionId, selectedOption, newCrossed).then(() => {});
  }

  const handleCheckAnswer = (i: number) => {
    setSelectedOption(i);
    api.session.setSingleChoiceAnswer(sessionId, localQuestionId, i, crossedAnswers).then(() => {});
  }

  return (
    <>
      <SingleChoiceQuestionDisplay
        question={question}
        showSolution={sessionType === 'practice' && (isFinished || false)}
        disabled={isFinished || false}
        selectedAnswer={selectedOption}
        crossedAnswers={crossedAnswers}
        onSelectAnswer={handleCheckAnswer}
        onCrossAnswer={handleCrossAnswer}
      />
    </>
  )

}