import React, {useState} from 'react';
import {Button} from 'react-bootstrap';
import SingleChoiceQuestionDisplay from './SingleChoiceQuestionDisplay';
import {SingleChoiceQuestion} from "../../api/question";
import {Link} from "react-router-dom";


interface MultipleQuestionProps {
  question: SingleChoiceQuestion
}

/**
 * Modal to display a single choice question.
 */
export default function SingleChoiceQuestionPreview(props: MultipleQuestionProps) {

  const {question} = props;

  const [selectedOption, setSelectedOption] = useState(-1);
  const [showSolution, setShowSolution] = useState(false);
  const [crossedAnswers, setCrossedAnswers] = useState<number[]>([])

  const handleCrossAnswer = (i: number) => {
    if (crossedAnswers.includes(i)) {
      setCrossedAnswers(ca => ca.filter(x => x !== i))
    } else {
      setCrossedAnswers(ca => [...ca, i])
    }
  }

  return (
    <>
      <SingleChoiceQuestionDisplay
        question={question}
        disabled={false}
        onSelectAnswer={setSelectedOption}
        showSolution={showSolution}
        selectedAnswer={selectedOption}
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