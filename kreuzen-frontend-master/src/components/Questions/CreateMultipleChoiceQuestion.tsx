import React from 'react';
import { Form, Button, Table } from 'react-bootstrap';
import * as yup from "yup";
import { FieldArray, Formik, FormikHelpers } from "formik";
import api from "../../api";
import { AxiosError } from "axios";
import { FormikErrors } from "formik/dist/types";

interface MultipleChoiceQuestionInput {
  text: string
  additionalInformation: string
  points: number
  origin: string
  answers: MultipleChoiceAnswer[]
}

interface MultipleChoiceAnswer {
  text: string
  isCorrect: boolean
}

const MultipleChoiceQuestionValidationSchema = yup.object().shape({
  text: yup.string().required("Ein Fragentext wird benötigt"),
  additionalInformation: yup.string(),
  points: yup.number().min(1, "Eine Frage sollte mindestens 1 Punkt geben").required("Bitte gib die Punktzahl für die Aufgabe ein."),
  origin: yup.string().required("Die Herkunft der Frage muss angegeben werden."),
  answers: yup.array()
    .min(2, "Eine Single Choice Frage sollte mindestens 2 Antwortmöglichkeiten haben.")
    .of(yup.object().shape({
      text: yup.string().required("Ein Antwortstext ist erforderlich"),
      isCorrect: yup.boolean()
    }))
})

interface MultipleQuestionProps {
  onEntered: () => void
  onCancel: () => void
  courseId: number
  examId?: number | null
}

/**
 * Modal to enter a multiple choice question.
 */
export default function EnterSingleQuestion(props: MultipleQuestionProps) {

  const { onEntered, onCancel, courseId, examId } = props;

  const printAnswerError = (x: string | FormikErrors<{ text: string, isCorrect: boolean }>) => {
    if (x instanceof String) {
      return x;
    } else {
      const err: FormikErrors<{ text: string, isCorrect: boolean }> = x as FormikErrors<{ text: string, isCorrect: boolean }>;
      return err.text;
    }
  }

  return (
    <Formik
      initialValues={{
        text: '',
        additionalInformation: '',
        points: 1,
        origin: 'ORIG',
        answers: [
          { text: '', isCorrect: false },
          { text: '', isCorrect: false },
          { text: '', isCorrect: false },
          { text: '', isCorrect: false },
        ]
      }}
      onSubmit={(
        values: MultipleChoiceQuestionInput,
        { setSubmitting, setFieldError, resetForm }: FormikHelpers<MultipleChoiceQuestionInput>
      ) => {
        setSubmitting(true);
        api.question.createMultipleChoiceQuestion(
          courseId,
          examId || null,
          values.text,
          values.additionalInformation,
          values.origin,
          values.points,
          values.answers
        ).then(() => {

          setSubmitting(false);
          resetForm();
          onEntered();
        }).catch((err: AxiosError) => {
          setFieldError('origin', '');
          setFieldError('text', '');
          setFieldError('additionalInformation', '');
          setFieldError('points', '');
          setFieldError('answers', err?.response?.data?.msg || 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
          setSubmitting(false);
        })
      }}
      validationSchema={MultipleChoiceQuestionValidationSchema}
      validateOnBlur={false}
      validateOnChange={false}
    >
      {
        ({
          values,
          errors,
          handleChange,
          handleSubmit,
          setFieldError,
          isSubmitting
        }) => (
          <Form onSubmit={handleSubmit}>
            <Form.Group controlId="origin">
              <Form.Label><b>Fragenherkunft</b></Form.Label>
              <Form.Control
                as="select"
                name="origin"
                placeholder="Fragentyp"
                disabled={isSubmitting}
                value={values.origin}
                onChange={handleChange}
                isInvalid={errors.origin != null}
              >
                <option value="ORIG">Originalfrage</option>
                <option value="GEPR">Gedächtnisprotokoll-Frage</option>
                <option value="NIKL">Nicht-Klausur-Frage</option>
                <option value="IMPP">IMPP-Frage</option>
              </Form.Control>
              {errors.origin && (
                <Form.Control.Feedback type="invalid">{errors.origin}</Form.Control.Feedback>
              )}
            </Form.Group>
            <Form.Group controlId="text">
              <Form.Label><b>Fragentext</b></Form.Label>
              <Form.Control
                as="textarea"
                rows={8}
                name="text"
                disabled={isSubmitting}
                value={values.text}
                onChange={handleChange}
                isInvalid={errors.text != null}
              />
              {errors.text && (
                <Form.Control.Feedback type="invalid">{errors.text}</Form.Control.Feedback>
              )}
            </Form.Group>
            <Form.Group controlId="additional-information">
              <Form.Label><b>Zusätzliche Informationen (optional)</b></Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                name="additionalInformation"
                disabled={isSubmitting}
                value={values.additionalInformation}
                onChange={handleChange}
                isInvalid={errors.additionalInformation != null}
              />
              {errors.additionalInformation && (
                <Form.Control.Feedback type="invalid">{errors.additionalInformation}</Form.Control.Feedback>
              )}
            </Form.Group>
            <Form.Group controlId="punkte">
              <Form.Label><b>Punkte</b></Form.Label>
              <Form.Control
                type="number"
                min="0"
                name="points"
                disabled={isSubmitting}
                value={values.points}
                onChange={handleChange}
                isInvalid={errors.points != null}
              />
              {errors.points && (
                <Form.Control.Feedback type="invalid">{errors.points}</Form.Control.Feedback>
              )}
            </Form.Group>

            <FieldArray
              name="answers"
              render={arrayHelpers => (
                <div>
                  <Table style={{ marginTop: 32 }} borderless>
                    <thead>
                      <tr>
                        <th style={{ width: 80 }}>Richtig?</th>
                        <th>Antwort</th>
                        <th style={{ width: 32 }} />
                      </tr>
                    </thead>
                    <tbody>
                      {
                        values.answers.map((answer, i) => (
                          <tr key={i}>
                            <td style={{ verticalAlign: 'middle', textAlign: 'center' }}>
                              <Form.Check
                                inline
                                type="checkbox"
                                name={`answers.${i}.isCorrect`}
                                checked={answer.isCorrect}
                                onChange={handleChange}
                                isInvalid={Array.isArray(errors.answers) && errors.answers[i] != null}
                                disabled={isSubmitting}
                              />
                            </td>
                            <td>
                              <Form.Control
                                name={`answers.${i}.text`}
                                onChange={handleChange}
                                value={answer.text}
                                placeholder="Antwortmöglichkeit"
                                disabled={isSubmitting}
                                isInvalid={Array.isArray(errors.answers) && errors.answers[i] != null}
                              />
                              {Array.isArray(errors.answers) && errors.answers[i] && (
                                <Form.Control.Feedback type="invalid">{printAnswerError(errors.answers[i])}</Form.Control.Feedback>
                              )}
                            </td>
                            <td>
                              <Button
                                variant="danger"
                                size="sm"
                                onClick={() => arrayHelpers.remove(i)}
                                disabled={isSubmitting}
                              >
                                <i className="fas fa-trash" />
                              </Button>
                            </td>
                          </tr>
                        ))
                      }
                    </tbody>
                  </Table>

                  {
                    !Array.isArray(errors.answers) && errors.answers && (
                      <p className="text-danger">{errors.answers}</p>
                    )
                  }

                  <Button
                    variant="success"
                    disabled={isSubmitting}
                    onClick={() => {
                      setFieldError('answers', undefined);
                      arrayHelpers.insert(values.answers.length, '');
                    }}
                  >
                    + Antwortmöglichkeit hinzufügen
                  </Button>
                </div>
              )}
            />

            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 48 }}>
              <Button variant="success" type="submit" disabled={isSubmitting}>
                Frage Hinzufügen
              </Button>
              <Button variant="secondary" onClick={onCancel} disabled={isSubmitting}>
                Zurück zu den Fragen
              </Button>
            </div>
          </Form>
        )
      }
    </Formik>
  )

}