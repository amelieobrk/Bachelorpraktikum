import React from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';

interface SemesterInput {
  semesterName: string
  semesterStartYear: number
  semesterEndYear: number
}

const SemesterValidationSchema = yup.object().shape({
  semesterName: yup.string().required("Bitte gib einen Semesternamen an. ( -> Beispiel: WiSe 2020/2021)"),
  semesterStartYear: yup.number().required("Bitte gib das Startjahr des Semesters an. ( -> Beispiel: 2020)"),
  semesterEndYear: yup.number().required("Bitte gib das Endjahr des Semesters an. ( -> Beispiel: 2021)"),
})

/**
 * Modal to create a new semester.
 */
export default function CreateSemesterModal(props : {isOpen: boolean, onClose: () => void, onCreated: () => void}) {

  const {isOpen, onClose, onCreated} = props;

  return (
    <Modal show={isOpen} onHide={onClose} transition={false}>
      <Modal.Header>
        <Modal.Title>Semester hinzufügen</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Formik
          initialValues={{
            semesterName: '',
            semesterStartYear: 2020,
            semesterEndYear: 2020
          }}
          onSubmit={(
            values: SemesterInput,
            { setSubmitting, setFieldError }: FormikHelpers<SemesterInput>
          ) => {
            setSubmitting(true);
            api.semester.createSemester(values.semesterName, values.semesterStartYear, values.semesterEndYear)
              .then(() => {
                setSubmitting(false);
                onCreated();
              })
              .catch((err) => {
                if (err.response && err.response.data && err.response.data.msg) {
                  setFieldError('semesterEndYear', err.response.data.msg);
                } else {
                  setFieldError('semesterEndYear', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
                }
                setFieldError('semesterEndYear', '');
                setSubmitting(false);
              });
          }}
          validationSchema={SemesterValidationSchema}
          validateOnBlur={false}
          validateOnChange={false}
        >
          {
            ({
               values,
               errors,
               handleChange,
               handleBlur,
               handleSubmit,
               isSubmitting
             }) => (
              <Form onSubmit={handleSubmit}>
                <Form.Group controlId="email">

                  <Form.Control
                    data-testid="semesterName-input"
                    autoFocus
                    isInvalid={errors.semesterName != null}
                    type="text"
                    value={values.semesterName}
                    name="semesterName"
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Bezeichnung des Semsters (Beispiel: WiSe20/21)"
                  />
                  {errors.semesterName && (
                    <Form.Control.Feedback type="invalid">{errors.semesterName}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="semesterStartYear">
                  <Form.Control
                    data-testid="semesterStartYear-input"
                    isInvalid={errors.semesterStartYear != null}
                    type="number"
                    name="semesterStartYear"
                    value={values.semesterStartYear}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Startjahr des Semesters"
                  />
                  {errors.semesterStartYear && (
                    <Form.Control.Feedback type="invalid">{errors.semesterStartYear}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="semesterEndYear">
                  <Form.Control
                    data-testid="semesterEndYear-input"
                    isInvalid={errors.semesterEndYear != null}
                    type="number"
                    name="semesterEndYear"
                    value={values.semesterEndYear}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Endjahr des Semesters"
                  />
                  {errors.semesterEndYear && (
                    <Form.Control.Feedback type="invalid">{errors.semesterEndYear}</Form.Control.Feedback>
                  )}
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
        <Button onClick={onClose}>Schließen</Button>
      </Modal.Footer>
    </Modal>
  );
}
