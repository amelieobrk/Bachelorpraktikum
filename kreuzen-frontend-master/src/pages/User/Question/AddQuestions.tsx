import React, {useEffect, useState} from 'react';
import { Module } from '../../../api/modules';
import {Course} from "../../../api/course";
import api from "../../../api";
import {Button, Card, Form} from "react-bootstrap";
import {Exam} from "../../../api/exam";
import {prettyPrintDate, sortByName} from "../../../utils";
import {Link, useHistory} from 'react-router-dom';
import CardHeader from "../../../components/General/CardHeader";


/*
* Returns a page that lets the user type questions
*
*/
export default function AddQuestions(props : {
  userId: number
}) {

  const {userId} = props;

  const history = useHistory();

  const [modules, setModules] = useState<Module[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [exams, setExams] = useState<Exam[]>([]);

  const [selectedModule, setSelectedModule] = useState<number>(0);
  const [selectedCourse, setSelectedCourse] = useState<number>(0);
  const [selectedExam, setSelectedExam] = useState<number>(0);
  const [enterMultiple, setEnterMultiple] = useState(true);

  useEffect(() => {
    api.module.getModulesByUser(userId).then(setModules)
  }, [userId])
  useEffect(() => {
    setSelectedCourse(0);
    setSelectedExam(0);
    if (selectedModule !== 0) {
      api.course.getCoursesOfModule(selectedModule).then(setCourses)
    } else {
      setCourses([]);
    }
  },[selectedModule])
  useEffect(() => {
    setSelectedExam(0);
    if (selectedCourse !== 0) {
      api.exam.getByCourse(selectedCourse).then(setExams)
    } else {
      setExams([])
    }
  }, [selectedCourse])

  return (
    <>
      <Card style={{marginTop: 32}}>
        <Card.Body>
          <CardHeader text="Frage(n) eingeben" secondary />

          <Form.Group controlId="moduleId">
            <Form.Control
              as="select"
              name="moduleId"
              value={selectedModule}
              onChange={(e) => {
                setSelectedModule(parseInt(e.target.value));
              }}
              placeholder="Modul"
            >
              <option value={0}>Module</option>
              {
                modules.sort(sortByName).map(module => (
                  <option value={module.id} key={module.id}>{module.name}</option>
                ))
              }
            </Form.Control>
          </Form.Group>

          <Form.Group controlId="courseId">
            <Form.Control
              as="select"
              name="courseId"
              value={selectedCourse}
              onChange={(e) => {
                setSelectedCourse(parseInt(e.target.value));
              }}
              placeholder="Kurs"
              disabled={selectedModule === 0}
            >
              <option value={0}>Kurs</option>
              {
                courses.map(course => (
                  <option value={course.id} key={course.id}>{course.name}</option>
                ))
              }
            </Form.Control>
          </Form.Group>

          <Form.Group controlId="examId">
            <Form.Control
              as="select"
              name="examId"
              value={selectedExam}
              onChange={(e) => {
                setSelectedExam(parseInt(e.target.value));
              }}
              placeholder="Klausur (optional)"
              disabled={selectedModule === 0}
            >
              <option value={0}>Klausur (optional)</option>
              {
                exams.map(exam => (
                  <option value={exam.id} key={exam.id}>{exam.name} am {prettyPrintDate(exam.date)}</option>
                ))
              }
            </Form.Control>
          </Form.Group>

          <Form.Check
            type="switch"
            id="enterMultiple"
            label="Mehrere Fragen eingeben?"
            checked={enterMultiple}
            onChange={() => setEnterMultiple(x => !x)}
          />

          <div style={{display: 'flex', justifyContent: 'space-between', marginTop: 32}}>
            <Button
              variant="success"
              disabled={selectedCourse === 0}
              onClick={() => {
                const params = new URLSearchParams();
                let setParam = false;
                if (selectedExam !== 0) {
                  params.append('exam', String(selectedExam));
                  setParam = true;
                }
                if (enterMultiple) {
                  params.append('multi', 'true');
                  setParam = true;
                }
                history.push(`/user/questions/add/${selectedCourse}${setParam ? '?' + params.toString() : ''}`)
              }}
            >
              Frage{enterMultiple ? 'n' : ''} eingeben
            </Button>
            <Button
              variant="secondary"
              as={Link}
              to="/user/questions"
            >
              Zurück
            </Button>
          </div>

        </Card.Body>
      </Card>
    </>
  );
}