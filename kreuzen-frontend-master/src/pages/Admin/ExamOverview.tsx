import React, { useCallback, useEffect, useState } from 'react';
import api from "../../api";
import { Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import { AxiosError } from 'axios';
import { Semester } from "../../api/semester";
import { Major } from "../../api/university";
import { University } from "../../api/auth";
import { Course } from "../../api/course";
import { Exam } from "../../api/exam";
import { prettyPrintDate, sortByDate, sortByName } from "../../utils";
import CreateExamModal from "../../components/Exam/CreateExamModal";
import ExamModal from "../../components/Exam/ExamModal";
import CardHeader from "../../components/General/CardHeader";

interface CourseWithExam {
  course: Course
  exams: Exam[]
}

/**
 * List of all exams.
 */
export default function ExamOverview() {

  const [addExamOpen, setAddExamOpen] = useState(false);
  const [openExam, setOpenExam] = useState<number | null>(null);

  const [exams, setExams] = useState<CourseWithExam[]>([]);
  const [error, setError] = useState<null | string>(null);

  const [universities, setUniversities] = useState<University[]>([]);
  const [majors, setMajors] = useState<Major[]>([]);
  const [semesters, setSemesters] = useState<Semester[]>([]);

  const [selectedUniversity, setSelectedUniversity] = useState(0);
  const [selectedMajor, setSelectedMajor] = useState(0);
  const [selectedSemester, setSelectedSemester] = useState(0);

  useEffect(() => {
    // Load universities
    api.university.getUniversities().then(setUniversities)
    // Load semesters
    api.semester.getSemesters().then(setSemesters)
  }, [])

  useEffect(() => {
    setSelectedMajor(0);
    if (selectedUniversity !== 0) {
      api.university.getMajorsByUniversityId(selectedUniversity).then(setMajors)
    } else {
      setSelectedSemester(0);
      setMajors([]);
    }
  }, [selectedUniversity])

  const loadExams = useCallback(() => {
    setError(null);

    const processExams = async (ex: Exam[]) => {
      const courseIds: number[] = Array.from(new Set(ex.map(e => e.courseId))); // Map is used to get values only once
      const courses: Course[] = (await Promise.all(
        courseIds.map(course => api.course.getCourse(course))
      )).sort(sortByName);
      const coursesWithExams: CourseWithExam[] = courses.map(course => ({
        course,
        exams: ex.filter(e => e.courseId === course.id).sort(sortByDate)
      }));
      setExams(coursesWithExams);
    }
    const handleError = (e: AxiosError) => {
      setError(e?.response?.data?.msg || "Die Daten konnten nicht geladen werden.");
    }

    if (selectedUniversity) {
      if (selectedSemester) {
        if (selectedMajor) {
          api.exam.getBySemesterAndMajor(selectedUniversity, selectedSemester, selectedMajor).then(processExams).catch(handleError);
        } else {
          api.exam.getBySemester(selectedUniversity, selectedSemester).then(processExams).catch(handleError);
        }
      } else if (selectedMajor) {
        api.exam.getByMajor(selectedUniversity, selectedMajor).then(processExams).catch(handleError);
      } else {
        api.exam.getByUniversity(selectedUniversity).then(processExams).catch(handleError);
      }
    }
  }, [selectedUniversity, selectedMajor, selectedSemester])

  useEffect(() => {
    setExams([]);
    loadExams();
  }, [loadExams])

  return (
    <>
      <Card>
        <Card.Body>
          <CardHeader
            text="Klausurverwaltung"
            actions={
              (
                <Button variant="primary" onClick={() => setAddExamOpen(true)}>
                  &nbsp;+&nbsp;
                </Button>
              )
            }
          />
          <Row>
            <Col>
              <Form.Group>
                <label htmlFor="universität"><h2>Universität</h2></label>
                <Form.Control
                  as="select"
                  name="universityId"
                  value={selectedUniversity}
                  id="universität"
                  onChange={(e) => setSelectedUniversity(parseInt(e.target.value))}
                  placeholder="Universität"
                >
                  <option value={0}>Universität</option>
                  {
                    universities.map(uni => (
                      <option value={uni.id} key={uni.id}>{uni.name}</option>
                    ))
                  }
                </Form.Control>
              </Form.Group>
            </Col>
            <Col>
              <Form.Group>
                <label htmlFor="studiengang"><h2>Studiengang</h2></label>
                <Form.Control
                  as="select"
                  name="majorId"
                  value={selectedMajor}
                  disabled={selectedUniversity === 0}
                  id="studiengang"
                  onChange={(e) => setSelectedMajor(parseInt(e.target.value))}
                  placeholder="Studiengang"
                >
                  <option value={0}>Alle Studiengänge</option>
                  {
                    majors.map(major => (
                      <option value={major.id} key={major.id}>{major.name}</option>
                    ))
                  }
                </Form.Control>
              </Form.Group>
            </Col>
          </Row>
          <Row>
            <Col>
              <Form.Group>
                <label htmlFor="semester"><h2>Semester</h2></label>
                <Form.Control
                  as="select"
                  name="semesterId"
                  disabled={selectedUniversity === 0}
                  value={selectedSemester}
                  id="semester"
                  onChange={(e) => setSelectedSemester(parseInt(e.target.value))}
                  placeholder="Semester"
                >
                  <option value={0}>Alle Semester</option>
                  {
                    semesters.map(semester => (
                      <option value={semester.id} key={semester.id}>{semester.name}</option>
                    ))
                  }
                </Form.Control>
              </Form.Group>
            </Col>
          </Row>
          <Card.Footer>
            {
              error ? (
                error
              ) : exams.length === 0 ? (
                <>
                  Es wurden keine Klausuren gefunden.
              </>
              ) : (
                exams.map(exam => (
                  <div key={exam.course.id} style={{ marginTop: 32 }}>
                    <CardHeader text={exam.course.name} secondary />
                    <Table hover>
                      <thead>
                        <tr>
                          <th>#</th>
                          <th>Datum</th>
                          <th>Eingetragene Fragen</th>
                          <th>Vollständig?</th>
                        </tr>
                      </thead>
                      <tbody>
                        {
                          exam.exams.map((e, i) => (
                            <tr
                              key={e.id}
                              onClick={() => setOpenExam(e.id)}
                              style={{ cursor: 'pointer' }}
                            >
                              <td>{(i + 1)}</td>
                              <td>{prettyPrintDate(e.date)}</td>
                              <td>?</td>
                              <td>
                                {
                                  e.isComplete ? (
                                    <i className="fas fa-check text-success" />
                                  ) : (
                                    <i className="fas fa-times text-danger" />
                                  )
                                }
                              </td>
                            </tr>
                          ))
                        }
                      </tbody>
                    </Table>
                  </div>
                ))
              )
            }
          </Card.Footer>

        </Card.Body>
      </Card>
      <CreateExamModal
        isOpen={addExamOpen}
        onClose={() => setAddExamOpen(false)}
        onCreated={() => {
          loadExams();
          setAddExamOpen(false);
        }}
      />
      <ExamModal
        onChanged={loadExams}
        examId={openExam}
        isOpen={true}
        onClose={() => setOpenExam(null)}
      />
    </>
  );
}
