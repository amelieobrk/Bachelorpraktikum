import React from 'react';
import { Button, Form } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';

interface EditExamInput {
  name: string
  date: string
  isRetry: boolean
  isComplete: boolean
}

const EditExamValidationSchema = yup.object().shape({
  name: yup.string().required("Bitte wähle einen Namen aus."),
  date: yup.string().required("Bitte wähle ein Datum aus."),
  isRetry: yup.boolean(),
  isComplete: yup.boolean()
})

/**
 * Form to edit an exam.
 */
export default function EditExam(props : {
  id: number,
  name: string,
  date: Date,
  isRetry: boolean,
  isComplete: boolean,
  onUpdated: () => void
}) {

  const {id, name, date, isRetry, isComplete, onUpdated} = props;

  return (
    <Formik
      initialValues={{
        name: name || '',
        date: date.toISOString().substring(0, 10) || '',
        isRetry: isRetry || false,
        isComplete: isComplete || false
      }}
      onSubmit={(
        values: EditExamInput,
        { setSubmitting, setFieldError, resetForm }: FormikHelpers<EditExamInput>
      ) => {
        setSubmitting(true);
        api.exam.updateExam(id, values.date, values.name, values.isRetry, values.isComplete)
          .then(() => {
            setSubmitting(false)
            resetForm();
            onUpdated();
          })
          .catch((err) => {
            setFieldError('name', '');
            setFieldError('date', err?.response?.data?.msg || 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
            setSubmitting(false);
          });
      }}
      validationSchema={EditExamValidationSchema}
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
              />
              {errors.isRetry && (
                <Form.Control.Feedback type="invalid">{errors.isRetry}</Form.Control.Feedback>
              )}
            </Form.Group>

            <Form.Group controlId="isComplete">
              <Form.Check
                name="isComplete"
                label="Alle Fragen eingetragen?"
                onChange={handleChange}
                checked={values.isComplete}
              />
              {errors.isComplete && (
                <Form.Control.Feedback type="invalid">{errors.isComplete}</Form.Control.Feedback>
              )}
            </Form.Group>

            <Button
              data-testid="submit-button"
              block
              type="submit"
              disabled={isSubmitting}
              variant="primary"
            >
              Speichern
            </Button>
          </Form>
        )
      }
    </Formik>
  );
}
