import React, {useEffect, useState} from 'react';
import {Card, Nav} from "react-bootstrap";
import {Switch, Route, Redirect, NavLink, useHistory} from "react-router-dom";
import CreateSingleChoiceQuestion from '../../../components/Questions/CreateSingleChoiceQuestion';
import {Course} from "../../../api/course";
import {Exam} from "../../../api/exam";
import api from "../../../api";
import {prettyPrintDate} from "../../../utils";
import CreateMultipleChoiceQuestion from '../../../components/Questions/CreateMultipleChoiceQuestion';
import CardHeader from "../../../components/General/CardHeader";

/*
* Selection of different options for adding Questions
*
*/
export default function AddQuestionsSpecific(props : {
  courseId: number
}) {

  const history = useHistory();
  const params = new URLSearchParams(history.location.search);

  const {courseId} = props;
  const examId : number = parseInt(params.get('exam') || '0');
  const multipleQuestions : boolean = params.get('multi') === 'true';

  const [course, setCourse] = useState<Course | null>(null);
  const [exam, setExam] = useState<Exam | null>(null);

  useEffect(() => {
    if (courseId !== 0) {
      api.course.getCourse(courseId).then(setCourse);
    } else {
      setCourse(null)
    }
  }, [courseId])
  useEffect(() => {
    if (examId !== 0) {
      api.exam.getExam(examId).then(setExam)
    } else {
      setExam(null)
    }
  }, [examId])

  const onEnteredQuestion = () => {
    if (!multipleQuestions) {
      history.push('/user/questions');
    }
  }

  return (
    <>
      <Card>
        <Card.Body>
          <CardHeader text="Frage eingeben" secondary />

          <b>Kurs:</b> {course?.name || "Loading..."}<br/>
          <b>Klausur:</b>&nbsp;
          {
            examId === 0 ? 'Keine ausgewählt' : (
              exam ? `${exam?.name || "Klausur"} am ${prettyPrintDate(exam.date)}` : "Loading..."
            )
          }<br/>
          <b>Mehrfacheingabe:</b> {multipleQuestions ? 'aktiv' : 'inaktiv'}
        </Card.Body>
        <Card.Footer>
          <b>Tipps zum Eingeben von Fragen:</b>
          <ul>
            <li>Nur Fragen eingeben, die tatsächlich in einer Klausur vorkamen.</li>
            <li>Nur Fragen eingeben, bei deren Antwort du dir sicher bist.</li>
            <li>Die Fragen werden manuell überprüft, d.h. es kann einige Tage dauern, bis sie tatsächlich im Fragenverzeichnis freigeschaltet sind.</li>
          </ul>
        </Card.Footer>
      </Card>
      <Card style={{marginTop: 32}}>
        <Card.Header>
          <Nav variant="tabs">
            <Nav.Item>
              <Nav.Link as={NavLink} to={`/user/questions/add/${courseId}/single-choice${history.location.search}`}>
                Single-Choice
              </Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link as={NavLink} to={`/user/questions/add/${courseId}/multiple-choice${history.location.search}`}>
                Multiple-Choice
              </Nav.Link>
            </Nav.Item>
          </Nav>
        </Card.Header>
        <Card.Body>
          <Switch>
            <Route path="/user/questions/add/:courseId/single-choice">
              <CreateSingleChoiceQuestion
                courseId={courseId}
                examId={examId}
                onCancel={() => history.push("/user/questions")}
                onEntered={onEnteredQuestion}
              />
            </Route>
            <Route path="/user/questions/add/:courseId/multiple-choice">
              <CreateMultipleChoiceQuestion
                courseId={courseId}
                examId={examId}
                onCancel={() => history.push("/user/questions")}
                onEntered={onEnteredQuestion}
              />
            </Route>
            <Redirect to={`/user/questions/add/:courseId/single-choice${history.location.search}`} />
          </Switch>
        </Card.Body>
      </Card>
    </>
  );
}