import React, {useEffect, useState} from 'react';
import {MultipleChoiceQuestion, SingleChoiceQuestion} from "../../../api/question";
import api from "../../../api";
import {Button, Card} from "react-bootstrap";
import SingleChoiceQuestionPreview from "../../../components/Questions/SingleChoiceQuestionPreview";
import {Link} from "react-router-dom";
import MultipleChoiceQuestionPreview from "../../../components/Questions/MultipleChoiceQuestionPreview";
import CommentBox from "../../../components/Questions/Comment/CommentBox";
import {Course} from "../../../api/course";
import {Exam} from "../../../api/exam";
import {prettyPrintDate} from "../../../utils";
import CardHeader from "../../../components/General/CardHeader";

/*
*
* Displays a selected Questions (+ details)
*
*/
export default function QuestionSpecific(props : {
  questionId: number
  userId: number
}) {

  const {questionId, userId} = props;

  const [question, setQuestion] = useState<SingleChoiceQuestion | MultipleChoiceQuestion | null>(null);
  const [course, setCourse] = useState<Course | null>(null)
  const [exam, setExam] = useState<Exam | null>(null)

  const courseId : number = question?.courseId || 0;
  const examId : number = question?.examId || 0;

  useEffect(() => {
    if (courseId !== 0) {
      api.course.getCourse(courseId).then(setCourse)
    } else {
      setCourse(null)
    }
  }, [courseId])
  useEffect(() => {
    if (examId !== 0) {
      api.exam.getExam(examId).then(setExam)
    } else {
      setCourse(null)
    }
  }, [examId])

  useEffect(() => {
    api.question.getQuestion(questionId).then(setQuestion)
  }, [questionId])

  const renderQuestionType = () => {
    if (!question) {
      return null;
    }
    switch (question.type.toLowerCase()) {
      case 'single-choice':
        return (
          <SingleChoiceQuestionPreview question={question as SingleChoiceQuestion} />
        )
      case 'multiple-choice':
        return (
          <MultipleChoiceQuestionPreview question={question as MultipleChoiceQuestion} />
        )
      default:
        return "Fragentyp kann nicht angezeigt werden."
    }
  }

  const questionTypeString : string | null = question && (
    question.type === 'single-choice'
      ? '(Single Choice)'
      : (
        question.type === 'multiple-choice'
          ? '(Multiple Choice)'
          : ''
      )
  )

  return (
    <>
      <Card>
        <Card.Body>
          <CardHeader
            text={`Frage #${questionId} ${questionTypeString}`}
            actions={
              <Button as={Link} to="/user/questions" variant="secondary">
                <i className="fas fa-arrow-left" />
              </Button>
            }
            secondary
          />

          <b>Kurs:</b> {course?.name || 'Loading'} <br/>
          {
            question?.examId && (
              <>
                <b>Klausur:</b> {exam?.name} am {exam?.date ? prettyPrintDate(exam?.date) : 'Loading...'}
              </>
            )
          }

          <hr/>

          {
            question == null ? (
              "Loading..."
            ) : (
              renderQuestionType()
            )
          }

        </Card.Body>
      </Card>
      <CommentBox questionId={questionId} userId={userId} />
    </>
  )
}