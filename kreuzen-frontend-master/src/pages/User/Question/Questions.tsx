import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Form, ListGroup, Row } from 'react-bootstrap';
import { Module } from "../../../api/modules";
import { Course } from "../../../api/course";
import api from "../../../api";
import { Link } from "react-router-dom";
import { Exam } from "../../../api/exam";
import { prettyPrintDate, shortenText, sortByDate, sortByName } from "../../../utils";
import { Question } from "../../../api/question";
import { PAGE_SIZE } from "../../../api/user";
import { Tag } from "../../../api/tags";
import axios, {Canceler} from "axios";

let cancelToken: Canceler | undefined;
let timeoutToken: number;

// Timespan between last change and the sending of the request. Used to take load from the backend.
const TYPING_TIMEOUT = 500;

export default function Questions(props: {
  userId: number
}) {

  const { userId } = props;

  const [modules, setModules] = useState<Module[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [exams, setExams] = useState<Exam[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [questionCount, setQuestionCount] = useState<number>(0);
  const [page, setPage] = useState<number>(0);

  const [selectedModule, setSelectedModule] = useState<number>(0);
  const [selectedCourse, setSelectedCourse] = useState<number>(0);
  const [selectedExam, setSelectedExam] = useState<number>(0);
  const [selectedTag, setSelectedTag] = useState<number>(0);
  const [searchString, setSearchString] = useState<string>('');

  useEffect(() => {
    api.module.getModulesByUser(userId).then(setModules)
  }, [userId])
  useEffect(() => {
    setSelectedCourse(0);
    setSelectedTag(0);
    if (selectedModule !== 0) {
      api.course.getCoursesOfModule(selectedModule).then(setCourses)
      api.tag.getTags(selectedModule).then(setTags)
    } else {
      setCourses([]);
      setTags([]);
    }
  }, [selectedModule])
  useEffect(() => {
    setSelectedExam(0);
    if (selectedCourse !== 0) {
      api.exam.getByCourse(selectedCourse).then(setExams)
    } else {
      setExams([]);
    }
  }, [selectedCourse])
  useEffect(() => {
    cancelToken && cancelToken(); // Cancel old requests..
    if (timeoutToken) clearTimeout(timeoutToken);
    timeoutToken = setTimeout(() => {
      setQuestions([]);
      api.question.getQuestions(
        page,
        selectedModule,
        selectedTag,
        selectedCourse,
        selectedExam,
        searchString,
        new axios.CancelToken((c) => {
          cancelToken = c
        })
      ).then(res => {
        setQuestionCount(res.count);
        setQuestions(res.entities);
      });
    }, TYPING_TIMEOUT)
  }, [page, selectedModule, selectedTag, selectedCourse, selectedExam, searchString]);
  useEffect(() => {
    setPage(0);
  }, [selectedModule, selectedTag, selectedCourse, selectedExam, searchString])

  const maxPage: number = Math.floor(questionCount / PAGE_SIZE);

  const getPageButtons = (): number[] => {
    const pages: number[] = [];
    if (page - 1 >= 0) {
      pages.push(page - 1);
    }
    pages.push(page);
    if (page + 1 <= maxPage) {
      pages.push(page + 1)
    }
    return pages;
  }

  const nextPage = () => {
    setPage(p => p + 1);
    setQuestions([]);
  }
  const prevPage = () => {
    setPage(p => p - 1);
    setQuestions([]);
  }
  const jumpPage = (p: number) => {
    setPage(p);
    setQuestions([]);
  }

  /*
  * Shows all Questions
  *
  */
  return (
    <>
      <Card>
        <Card.Body>
          <Row>
            <Col xs={12} md={8}>
              Du hast eine Klausur geschrieben?<br />
                Hilf deinen Kommilitonen beim Lernen, indem du die Fragen mit ihnen teilst!
            </Col>
            <Col xs={6} md={4}>
              <div style={{ display: 'flex', flex: 1, justifyContent: 'flex-end' }}>
                <Button variant="success" as={Link} to="/user/questions/add">
                  Frage(n) eingeben
                </Button>
              </div>
            </Col>
          </Row>
        </Card.Body>
      </Card>
      <Card style={{ marginTop: 32 }}>
        <Card.Body>
          <Card.Title>
            <Row>
              <Col>
                <h1>Fragenkatalog</h1>
              </Col>
              <Col>
                <Form.Group controlId="searchTerm" style={{ marginTop: 32 }}>
                  <Form.Control
                    name="searchTerm"
                    onChange={(e) => setSearchString(e.target.value)}
                    value={searchString}
                    placeholder="Suchetext..."
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Title>
          <Row>
            <Col>
              <Card>
                <Card.Body>
                  <Card.Title>
                    Erweiterte Suche
                  </Card.Title>
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
                      <option value={0}>Modul auswählen</option>
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
                      <option value={0}>Kurs auswählen</option>
                      {
                        courses.sort(sortByName).map(course => (
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
                      placeholder="Klausur"
                      disabled={selectedCourse === 0}
                    >
                      <option value={0}>Klausur auswählen</option>
                      {
                        exams.sort(sortByDate).map(exam => (
                          <option value={exam.id} key={exam.id}>{exam.name} am {prettyPrintDate(exam.date)}</option>
                        ))
                      }
                    </Form.Control>
                  </Form.Group>
                  <br />
                  <Form.Group controlId="tagId">
                    <Form.Control
                      as="select"
                      name="tagId"
                      value={selectedTag}
                      onChange={(e) => {
                        setSelectedTag(parseInt(e.target.value));
                      }}
                      placeholder="Tag"
                      disabled={selectedModule === 0}
                    >
                      <option value={0}>Tag</option>
                      {
                        tags.sort(sortByName).map(tag => (
                          <option value={tag.id} key={tag.id}>{tag.name}</option>
                        ))
                      }
                    </Form.Control>
                  </Form.Group>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          <hr />


          <ListGroup variant="flush">
            {
              questions.map(question => (
                <ListGroup.Item key={question.id}>
                  <Link to={`/user/questions/${question.id}`}>
                    {question.id}: {shortenText(question.text)}
                  </Link>
                </ListGroup.Item>
              ))
            }
          </ListGroup>


          <Row style={{ marginTop: 20 }}>
            <Col>
              <Button block disabled={page === 0} onClick={prevPage}>
                Letzte Seite
              </Button>
            </Col>

            <Col style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {
                getPageButtons().map(p => (
                  <Button key={p} disabled={p === page} style={{ marginLeft: 8, marginRight: 8 }} onClick={() => jumpPage(p)}>
                    {p + 1}
                  </Button>
                ))
              }
            </Col>

            <Col>
              <Button block disabled={page === maxPage} onClick={nextPage}>
                Nächste Seite
              </Button>
            </Col>
          </Row>

        </Card.Body>
      </Card>
    </>
  )
}