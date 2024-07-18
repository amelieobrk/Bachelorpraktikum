import React, {useEffect, useState} from 'react';
import {MultipleChoiceQuestion} from "../../../api/question";
import {SingleChoiceSelection} from "../../../api/session";
import api from "../../../api";
import MultipleChoiceQuestionDisplay from "../../Questions/MultipleChoiceQuestionDisplay";


interface MultipleChoiceQuestionReviewProps {
  question: MultipleChoiceQuestion
  sessionId: number
  localQuestionId: number
}

/**
 * Modal to review a multiple choice question.
 */
export default function MultipleChoiceQuestionReview(props: MultipleChoiceQuestionReviewProps) {

  const {question, sessionId, localQuestionId} = props;

  const [selection, setSelection] = useState<SingleChoiceSelection[]>([]);

  useEffect(() => {
    api.session.getMultipleChoiceSelection(sessionId, localQuestionId).then(setSelection)
  }, [sessionId, localQuestionId])

  return (
    <>
      <MultipleChoiceQuestionDisplay
        question={question}
        showSolution
        disabled
        selectedAnswers={selection.filter(x => x.isChecked).map(x => x.localAnswerId)}
        crossedAnswers={selection.filter(x => x.isCrossed).map(x => x.localAnswerId)}
        onCrossAnswer={() => {}}
        onSelectAnswer={() => {}}
      />
    </>
  )

}