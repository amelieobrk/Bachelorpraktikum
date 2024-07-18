import { Formik, FormikHelpers } from 'formik';
import React, { useEffect, useState } from 'react';

import { Button, Modal, ListGroup, Form } from 'react-bootstrap';
import api from '../../api'
import { Course } from "../../api/course";
import { Semester } from "../../api/semester";

interface CourseProps {
  moduleID: number
  moduleName: string
  open: boolean
  onClose: () => void
}

interface CourseInput {
}

/**
 * Modal to manage the courses of a module.
 *
 * @param props
 */
export default function ModuleCourses(props: CourseProps) {

  // Regulation for showing the Menu to add a Course
  const [addCourse, setShowAddCourse] = React.useState(false);
  const showAddCourse = () => {
    setShowAddCourse(true);
  };
  const showCourseDetails = () => {
    setShowAddCourse(false);
  };

  const [deleting, setDeleting] = React.useState(false);

  const [semester, setSemester] = useState<Semester[]>([])
  const [selectedSemester, setSelectedSemester] = useState("-1");//Default: Semester auswählen anzeigen, wenn man ausdrückt ändert sich dieses Value
  const [allCourses, setAllCourses] = useState<Course[]>([])

  // Sets the Courses when loading the Modal for the first time
  useEffect(() => {
    api.course.getCoursesOfModule(props.moduleID)
      .then(
        (response: any) => {
          setAllCourses(response)
        })
      .catch((err) => {
        if (err.response && err.response.data && err.response.data.msg) {
          return (
            <div className="alert alert-danger" role="alert">
              Es ist ein Fehler beim Laden der Module aufgetreten, bitte versuchen Sie es später erneut!
            </div>
          )
        }
      })
  }, [props.moduleID])

  // Reloads the list of Courses if it could have changed
  function reloadCourses() {
    api.course.getCoursesOfModule(props.moduleID)
      .then(
        (response: any) => {
          setAllCourses(response)
        })
  }

  // Sets the Semesters when loading the Modal for the first time
  useEffect(() => {
    api.semester.getSemesters()
      .then(
        (res: any) => {
          setSemester(res);
        })
      .catch((err) => {
        if (err.response && err.response.data && err.response.data.msg) {
          return (
            <div className="alert alert-danger" role="alert">
              Es ist ein Fehler beim Laden der Semester aufgetreten, bitte versuchen Sie es später erneut.
            </div>
          )
        }
      })
  }, [])

  // Gets the Name of the Semester by ID
  function getSemesterName(semID: number): String {
    let name = ""
    semester.forEach((element: Semester) => {
      if (element.id === semID)
        name = element.name
    }
    )
    return name
  }

  // Checks if a Course with the given Semester exists
  function courseAlreadyExists(semesterID: number): Boolean {
    let bool = false
    allCourses.forEach((element: Course) => {
      if (element.semesterId === semesterID)
        bool = true
    })
    return bool
  }

  // Deletes a Course
  function deleteCourse(course: Course): void {
    api.course.deleteCourse(course.id)
      .then(() => {
        reloadCourses()
      })
      .catch((err) => {
        if (err.response && err.response.data && err.response.data.msg) {
          console.log(err.response.data.msg);
        } else {
          console.log("Fehler ohne Message")
        }
      });
  }

  // Function 
  function close(): void {
    props.onClose()
    setDeleting(false)
    setShowAddCourse(false)
  }

  return (
    <>
      <Modal show={props.open} onHide={close}>

        {
          addCourse ? (
            <>
              <Modal.Header>
                <Modal.Title>Kurs hinzufügen</Modal.Title>
              </Modal.Header>
              <Modal.Body>
                <Formik
                  initialValues={{
                  }}
                  onSubmit={(
                    values: CourseInput,
                    { setSubmitting }: FormikHelpers<CourseInput>
                  ) => {
                    setSubmitting(true);
                    api.course.createCourse(parseInt(selectedSemester), props.moduleID)
                      .then(() => {
                        setSubmitting(false)
                        reloadCourses()
                        showCourseDetails()
                      })
                      .catch((err) => {
                        if (err.response && err.response.data && err.response.data.msg) {
                          console.log("Error")
                        }
                        setSubmitting(false);
                      });
                  }}
                  validateOnBlur={false}
                  validateOnChange={false}
                >
                  {
                    ({
                      handleSubmit,
                      isSubmitting
                    }) => (
                      <Form onSubmit={handleSubmit}>
                        <Form.Group controlId="semesterSelection">
                          <Form.Label>Semester auswählen</Form.Label>
                          <Form.Control as="select" value={selectedSemester} onChange={e => setSelectedSemester(e.target.value)}>
                            <option value={-1}> Semester auswählen</option>
                            {
                              semester
                                .filter((element: Semester) => (!courseAlreadyExists(element.id)))
                                .map((element: Semester) => <option value={element.id} key={element.id}>{element.name}</option>)
                            }
                          </Form.Control>
                        </Form.Group>

                        <Button
                          data-testid="submit-button"
                          block
                          type="submit"
                          disabled={isSubmitting}
                          variant="primary"
                        >
                          Erstellen
                        </Button>
                      </Form>
                    )
                  }
                </Formik>
              </Modal.Body>
              <Modal.Footer>
                <Button onClick={showCourseDetails}>Zurück</Button>
                <Button onClick={close}>Schließen</Button>
              </Modal.Footer>
            </>
          ) : (
            <>
              <Modal.Header>
                <Modal.Title>
                  Kurse des Modules
                </Modal.Title>
                <Button className="ml-auto" variant={deleting ? 'danger' : 'secondary'} onClick={() => setDeleting(s => !s)}>
                  <i className="fas fa-trash" />
                </Button>
              </Modal.Header>
              <Modal.Body>
              <ListGroup variant="flush" style={{width: 470, height: 420, overflow: "auto"}}>
                  {allCourses.sort((a, b) => b.semesterId - a.semesterId).map((element: Course) =>
                    <ListGroup.Item key={element.id} >
                      <div style={{ display: "flex", alignItems: "center" }}>
                        {props.moduleName} ({(getSemesterName(element.semesterId))})
                        <Button
                          disabled={!deleting}
                          variant="danger"
                          style={{ marginLeft: "auto" }}
                          onClick={() => { deleteCourse(element) }
                          }>
                          Löschen
                        </Button>
                      </div>
                    </ListGroup.Item>
                  )}
                </ListGroup>
              </Modal.Body>
              <Modal.Footer>
                <Button onClick={showAddCourse} variant="success" className="mr-auto">+ Kurs hinzufügen</Button>
                <Button onClick={close}>Schließen</Button>
              </Modal.Footer>
            </>
          )
        }
      </Modal>
    </>
  )
}
