import React from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';


interface EditSemesterInput {
  newSemesterName: string
  newSemesterStartYear: number
  newSemesterEndYear: number
}

const EditSemesterValidationSchema = yup.object().shape({
  newSemesterName: yup.string().required("Bitte gib einen Semesternamen an. ( -> Beispiel: WiSe 2020/2021)"),
  newSemesterStartYear: yup.number().required("Bitte gib das Startjahr des Semesters an. ( -> Beispiel: 2020)"),
  newSemesterEndYear: yup.number().required("Bitte gib das Endjahr des Semesters an. ( -> Beispiel: 2021)"),
})

/**
 * Modal to edit a semester.
 */
export default function EditSemesterModal(props : {semesterId: number, name: string, startYear: number, endYear: number, isOpen: boolean, onClose: () => void, onEdited: () => void}) {

  const {isOpen, onClose, onEdited, semesterId, name, startYear, endYear} = props;

  return (
    <Modal show={isOpen} onHide={onClose} transition={false}>
      <Modal.Header>
        <Modal.Title>Semester bearbeiten</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Formik
          initialValues={{
            newSemesterName: name,
            newSemesterStartYear: startYear,
            newSemesterEndYear: endYear
          }}
          onSubmit={(
            values: EditSemesterInput,
            { setSubmitting, setFieldError }: FormikHelpers<EditSemesterInput>
          ) => {
            setSubmitting(true);
            api.semester.updateSemester(semesterId, values.newSemesterEndYear, values.newSemesterName, values.newSemesterStartYear)
              .then(() => {
                setSubmitting(false)
                onEdited()
              })
              .catch((err) => {
                if (err.response && err.response.data && err.response.data.msg) {
                  setFieldError('newSemesterEndYear', err.response.data.msg);
                } else {
                  setFieldError('newSemesterEndYear', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
                }
                setFieldError('newSemesterEndYear', '');
                setSubmitting(false);
              });
          }}
          validationSchema={EditSemesterValidationSchema}
          validateOnBlur={false}
          validateOnChange={false}
        >
          {
            ({
               values,
               errors,
               touched,
               handleChange,
               handleBlur,
               handleSubmit,
               isSubmitting
             }) => (
              <Form onSubmit={handleSubmit}>
                <Form.Group controlId="newSemesterName">

                  <Form.Control
                    data-testid="newSemesterName-input"
                    autoFocus
                    isInvalid={errors.newSemesterName != null}
                    type="text"
                    value={values.newSemesterName}
                    name="newSemesterName"
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Bezeichnung des Semsters (Beispiel: WiSe20/21)"
                  />
                  {errors.newSemesterName && (
                    <Form.Control.Feedback type="invalid">{errors.newSemesterName}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="newSemesterStartYear">
                  <Form.Control
                    data-testid="newSemesterStartYear-input"
                    isInvalid={errors.newSemesterStartYear != null}
                    type="number"
                    name="newSemesterStartYear"
                    value={values.newSemesterStartYear}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Startjahr des Semesters"
                  />
                  {errors.newSemesterStartYear && (
                    <Form.Control.Feedback type="invalid">{errors.newSemesterStartYear}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="newSemesterEndYear">
                  <Form.Control
                    data-testid="newSemesterEndYear-input"
                    isInvalid={errors.newSemesterEndYear != null}
                    type="number"
                    name="newSemesterEndYear"
                    value={values.newSemesterEndYear}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Endjahr des Semesters"
                  />
                  {errors.newSemesterEndYear && (
                    <Form.Control.Feedback type="invalid">{errors.newSemesterEndYear}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Button
                  data-testid="submit-button"
                  block
                  type="submit"
                  disabled={isSubmitting}
                  variant="primary"
                >
                  Änderungen bestätigen
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
