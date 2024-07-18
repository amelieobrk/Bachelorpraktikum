import React, {useEffect, useState} from 'react';
import SingleChoiceQuestionDisplay from '../../Questions/SingleChoiceQuestionDisplay';
import {SingleChoiceQuestion} from "../../../api/question";
import {SingleChoiceSelection} from "../../../api/session";
import api from "../../../api";


interface SingleChoiceQuestionReviewProps {
  question: SingleChoiceQuestion
  sessionId: number
  localQuestionId: number
}

/**
 * Modal to review a single choice question.
 */
export default function SingleChoiceQuestionReview(props: SingleChoiceQuestionReviewProps) {

  const {question, sessionId, localQuestionId} = props;

  const [selection, setSelection] = useState<SingleChoiceSelection[]>([]);

  useEffect(() => {
    api.session.getSingleChoiceSelection(sessionId, localQuestionId).then(setSelection)
  }, [sessionId, localQuestionId])

  return (
    <>
      <SingleChoiceQuestionDisplay
        question={question}
        showSolution
        disabled
        selectedAnswer={selection.find(x => x.isChecked)?.localAnswerId || -1}
        crossedAnswers={selection.filter(x => x.isCrossed).map(x => x.localAnswerId)}
        onSelectAnswer={() => {}}
        onCrossAnswer={() => {}}
      />
    </>
  )

}