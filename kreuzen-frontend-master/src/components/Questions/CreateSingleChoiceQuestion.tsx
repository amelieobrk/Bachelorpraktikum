import React from 'react';
import { Form, Button, Table } from 'react-bootstrap';
import * as yup from "yup";
import { FieldArray, Formik, FormikHelpers } from "formik";
import api from "../../api";
import { AxiosError } from "axios";

interface SingleChoiceQuestionInput {
  text: string
  additionalInformation: string
  points: number
  origin: string
  answers: string[]
  correctAnswer: number
}

const SingleChoiceQuestionValidationSchema = yup.object().shape({
  text: yup.string().required("Ein Fragentext wird benötigt"),
  additionalInformation: yup.string(),
  points: yup.number().min(1, "Eine Frage sollte mindestens 1 Punkt geben").required("Bitte gib die Punktzahl für die Aufgabe ein."),
  origin: yup.string().required("Die Herkunft der Frage muss angegeben werden."),
  answers: yup.array().min(2, "Eine Single Choice Frage sollte mindestens 2 Antwortmöglichkeiten haben.").of(yup.string().required("Ein Antwortstext wird benötigt.")),
  correctAnswer: yup.number()
})

interface SingleQuestionProps {
  onEntered: () => void
  onCancel: () => void
  courseId: number
  examId?: number | null
}

/**
 * Modal to enter a single Questions.
 */
export default function EnterSingleQuestion(props: SingleQuestionProps) {

  const { onEntered, onCancel, courseId, examId } = props;

  return (
    <Formik
      initialValues={{
        text: '',
        additionalInformation: '',
        points: 1,
        origin: 'ORIG',
        answers: ['', '', '', ''],
        correctAnswer: 1
      }}
      onSubmit={(
        values: SingleChoiceQuestionInput,
        { setSubmitting, setFieldError, resetForm }: FormikHelpers<SingleChoiceQuestionInput>
      ) => {
        setSubmitting(true);
        api.question.createSingleChoiceQuestion(
          courseId,
          examId || null,
          values.text,
          values.additionalInformation,
          values.origin,
          values.points,
          values.answers,
          values.correctAnswer
        ).then(() => {

          setSubmitting(false);
          resetForm();
          onEntered();
        }).catch((err: AxiosError) => {
          setFieldError('origin', '');
          setFieldError('text', '');
          setFieldError('additionalInformation', '');
          setFieldError('points', '');
          values.answers.forEach((a, i) => setFieldError(`answers.${i}`, ''));
          setFieldError('correctAnswer', err?.response?.data?.msg || 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
          setSubmitting(false);
        })
      }}
      validationSchema={SingleChoiceQuestionValidationSchema}
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
                        <th style={{ width: 32 }}>#</th>
                        <th>Antworten</th>
                        <th style={{ width: 32 }} />
                      </tr>
                    </thead>
                    <tbody>
                      {
                        values.answers.map((answer, i) => (
                          <tr key={i}>
                            <td style={{ verticalAlign: 'middle' }}>
                              {i + 1}.
                            </td>
                            <td>
                              <Form.Control
                                name={`answers.${i}`}
                                onChange={handleChange}
                                value={answer}
                                placeholder="Antwortmöglichkeit"
                                disabled={isSubmitting}
                                isInvalid={Array.isArray(errors.answers) && errors.answers[i] != null}
                              />
                              {Array.isArray(errors.answers) && errors.answers[i] && (
                                <Form.Control.Feedback type="invalid">{errors.answers[i]}</Form.Control.Feedback>
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
                    style={{ marginBottom: 32 }}
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

            <Form.Group controlId="question-type">
              <Form.Label><b>Korrekte Antwort</b></Form.Label>
              <Form.Control
                as="select"
                name="correctAnswer"
                placeholder="Korrekte Antwort"
                disabled={isSubmitting}
                value={values.correctAnswer}
                onChange={handleChange}
                isInvalid={errors.correctAnswer != null}
              >
                {
                  values.answers.map((a, i) => (
                    <option value={i + 1} key={i}>Antwort #{i + 1}</option>
                  ))
                }
              </Form.Control>
              {errors.correctAnswer && (
                <Form.Control.Feedback type="invalid">{errors.correctAnswer}</Form.Control.Feedback>
              )}
            </Form.Group>

            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 48 }}>
              <Button variant="success" type="submit" disabled={isSubmitting}>
                Frage hinzufügen
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