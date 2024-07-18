import React, {ChangeEvent, useEffect, useState} from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';
import {University} from "../../api/auth";
import {Course} from "../../api/course";
import {Module} from "../../api/modules";

interface ExamInput {
  universityId: number
  courseId: number
  moduleId: number
  name: string
  date: string
  isRetry: boolean
}

const ExamValidationSchema = yup.object().shape({
  courseId: yup.number().min(1, "Bitte wähle einen Kurs aus.").required("Bitte wähle einen Kurs aus."),
  name: yup.string().required("Bitte wähle einen Namen aus."),
  date: yup.string().required("Bitte wähle ein Datum aus."),
  isRetry: yup.boolean()
})

/**
 * Component to select a module.
 */
const SelectModule = (props : {
  onChange: (event: ChangeEvent) => void
  value: number
  universityId: number
  isInvalid: boolean
}) => {

  const {onChange, value, universityId, isInvalid} = props;

  const [modules, setModules] = useState<Module[]>([]);
  useEffect(() => {
    if (universityId === 0) {
      setModules([])
    } else {
      api.module.getModuleByUniversity(universityId).then(setModules);
    }
  }, [universityId])

  return (
    <Form.Control
      data-testid="moduleId-input"
      isInvalid={isInvalid}
      as="select"
      name="moduleId"
      value={value}
      onChange={onChange}
      placeholder="Modul"
      disabled={universityId === 0}
    >
      <option value={0}>Modul</option>
      {
        modules.map(module => (
          <option value={module.id} key={module.id}>{module.name}</option>
        ))
      }
    </Form.Control>
  )
}

/**
 * Component to select a course
 */
const SelectCourse = (props : {
  onChange: (event: ChangeEvent) => void
  value: number
  moduleId: number
  isInvalid: boolean
}) => {

  const {onChange, value, moduleId, isInvalid} = props;

  const [courses, setCourses] = useState<Course[]>([]);
  useEffect(() => {
    if (moduleId === 0) {
      setCourses([])
    } else {
      api.course.getCoursesOfModule(moduleId).then(setCourses);
    }
  }, [moduleId])

  return (
    <Form.Control
      data-testid="courseId-input"
      isInvalid={isInvalid}
      as="select"
      name="courseId"
      value={value}
      onChange={onChange}
      placeholder="Kurs"
      disabled={moduleId === 0}
    >
      <option value={0}>Kurs</option>
      {
        courses.map(course => (
          <option value={course.id} key={course.id}>{course.name}</option>
        ))
      }
    </Form.Control>
  )
}

/**
 * Modal to create a new exam.
 * User has to enter: university, module, course/semester, name, date, isRetry
 */
export default function CreateExamModal(props : {isOpen: boolean, onClose: () => void, onCreated: () => void}) {

  const {isOpen, onClose, onCreated} = props;

  const [universities, setUniversities] = useState<University[]>([]);

  useEffect(() => {
    api.university.getUniversities().then(setUniversities);
  }, [])

  return (
    <Modal show={isOpen} onHide={onClose} transition="false">
      <Modal.Header>
        <Modal.Title>Klausur hinzufügen</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Formik
          initialValues={{
            universityId: 0,
            courseId: 0,
            moduleId: 0,
            name: '',
            date: '',
            isRetry: false
          }}
          onSubmit={(
            values: ExamInput,
            { setSubmitting, setFieldError, resetForm }: FormikHelpers<ExamInput>
          ) => {
            setSubmitting(true);
            api.exam.createExam(values.courseId, values.date, values.name, values.isRetry)
              .then(() => {
                setSubmitting(false)
                resetForm();
                onCreated();
              })
              .catch((err) => {
                setFieldError('universityId', '');
                setFieldError('courseId', '');
                setFieldError('moduleId', '');
                setFieldError('name', '');
                setFieldError('date', err?.response?.data?.msg || 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
                setSubmitting(false);
              });
          }}
          validationSchema={ExamValidationSchema}
          validateOnBlur={false}
          validateOnChange={false}
        >
          {
            ({
               values,
               setFieldValue,
               errors,
               handleChange,
               handleBlur,
               handleSubmit,
               isSubmitting
             }) => (
              <Form onSubmit={handleSubmit}>

                <Form.Group controlId="universityId">
                  <Form.Control
                    data-testid="universityId-input"
                    isInvalid={errors.universityId != null}
                    as="select"
                    name="universityId"
                    value={values.universityId}
                    onChange={(e) => {
                      handleChange(e);
                      setFieldValue('moduleId', 0);
                      setFieldValue('courseId', 0);
                    }}
                    onBlur={handleBlur}
                    placeholder="Universität"
                  >
                    <option value={0}>Universität</option>
                    {
                      universities.map(uni => (
                        <option value={uni.id} key={uni.id}>{uni.name}</option>
                      ))
                    }
                  </Form.Control>
                  {errors.universityId && (
                    <Form.Control.Feedback type="invalid">{errors.universityId}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="moduleId">
                  <SelectModule
                    universityId={values.universityId}
                    value={values.moduleId}
                    isInvalid={errors.moduleId != null}
                    onChange={(e) => {
                      handleChange(e);
                      setFieldValue('courseId', 0);
                    }}
                  />
                  {errors.moduleId && (
                    <Form.Control.Feedback type="invalid">{errors.moduleId}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="courseId">
                  <SelectCourse
                    moduleId={values.moduleId}
                    value={values.courseId}
                    isInvalid={errors.courseId != null}
                    onChange={handleChange}
                  />
                  {errors.courseId && (
                    <Form.Control.Feedback type="invalid">{errors.courseId}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="name">
                  <Form.Control
                    data-testid="name-input"
                    autoFocus
                    isInvalid={errors.name != null}
                    type="text"
                    value={values.name}
                    name="name"
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Name der Klausur"
                  />
                  {errors.name && (
                    <Form.Control.Feedback type="invalid">{errors.name}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="date">
                  <Form.Control
                    data-testid="date-input"
                    autoFocus
                    isInvalid={errors.date != null}
                    type="date"
                    value={values.date}
                    name="date"
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Datum der Klausur"
                  />
                  {errors.date && (
                    <Form.Control.Feedback type="invalid">{errors.date}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="isRetry">
                  <Form.Check
                    name="isRetry"
                    label="Wiederholungsklausur?"
                    onChange={handleChange}
                    checked={values.isRetry}
                    isInvalid={errors.isRetry != null}
                  />
                  {errors.isRetry && (
                    <Form.Control.Feedback type="invalid">{errors.isRetry}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Button
                  data-testid="submit-button"
                  block
                  type="submit"
                  disabled={isSubmitting || !values.date || values.courseId === 0}
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
        <Button onClick={onClose}>Schließen</Button>
      </Modal.Footer>
    </Modal>
  );
}
