import React, {useState} from 'react';
import {Button} from 'react-bootstrap';
import {MultipleChoiceQuestion} from "../../api/question";
import {Link} from "react-router-dom";
import MultipleChoiceQuestionDisplay from "./MultipleChoiceQuestionDisplay";


interface MultipleQuestionProps {
  question: MultipleChoiceQuestion
}

/**
 * Modal to display a multiple choice question.
 */
export default function MultipleChoiceQuestionPreview(props: MultipleQuestionProps) {

  const {question} = props;

  const [selectedOptions, setSelectedOptions] = useState<number[]>([]);
  const [showSolution, setShowSolution] = useState(false);
  const [crossedAnswers, setCrossedAnswers] = useState<number[]>([])

  const handleCrossAnswer = (i: number) => {
    if (crossedAnswers.includes(i)) {
      setCrossedAnswers(ca => ca.filter(x => x !== i))
    } else {
      setCrossedAnswers(ca => [...ca, i])
    }
  }

  const handleSelectAnswer = (i: number) => {
    if (selectedOptions.includes(i)) {
      setSelectedOptions(ca => ca.filter(x => x !== i))
    } else {
      setSelectedOptions(ca => [...ca, i])
    }
  }

  return (
    <>
      <MultipleChoiceQuestionDisplay
        question={question}
        disabled={false}
        onSelectAnswer={handleSelectAnswer}
        showSolution={showSolution}
        selectedAnswers={selectedOptions}
        onCrossAnswer={handleCrossAnswer}
        crossedAnswers={crossedAnswers}
      />

      <div style={{marginTop: 32, display: 'flex', justifyContent: 'space-between'}}>
        <Button onClick={() => setShowSolution(x => !x)}>
          {showSolution ? 'Korrekte Antwort verstecken' : 'Korrekte Antwort anzeigen'}
        </Button>
        <Button as={Link} to="/user/questions" variant="secondary">
          Zurück
        </Button>
      </div>
    </>
  )

}