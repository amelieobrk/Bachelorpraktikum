import React from 'react';
import {Button, Form} from "react-bootstrap";
import {Formik, FormikHelpers} from "formik";
import api from "../../../api";
import {AxiosError} from "axios";
import * as yup from "yup";

interface CommentInput {
  text: string
}

const CommentValidationSchema = yup.object().shape({
  text: yup.string().required("Bitte gib ein Kommentar ein, bevor du es postest.")
})

/**
 * Field to add a comment.
 */
export default function AddCommentField(props: {
  questionId: number
  onEntered: () => void
}) {

  const {questionId, onEntered} = props;

  return (
    <div style={{marginTop: 32}}>
      <Formik
        initialValues={{
          text: ''
        }}
        onSubmit={(
          values: CommentInput,
          { setSubmitting, setFieldError, resetForm }: FormikHelpers<CommentInput>
        ) => {
          setSubmitting(true);
          api.question.createComment(
            questionId,
            values.text
          ).then(() => {

            setSubmitting(false);
            resetForm();
            onEntered();
          }).catch((err : AxiosError) => {
            setFieldError('text', err?.response?.data?.msg || 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
            setSubmitting(false);
          })
        }}
        validationSchema={CommentValidationSchema}
        validateOnBlur={false}
        validateOnChange={false}
      >
        {
          ({
             values,
             errors,
             handleChange,
             handleSubmit,
             isSubmitting
           }) => (
            <Form onSubmit={handleSubmit}>
              <Form.Group controlId="text">
                <Form.Control
                  as="textarea"
                  rows={3}
                  name="text"
                  disabled={isSubmitting}
                  value={values.text}
                  onChange={handleChange}
                  isInvalid={errors.text != null}
                  placeholder="Kommentar"
                />
                {errors.text && (
                  <Form.Control.Feedback type="invalid">{errors.text}</Form.Control.Feedback>
                )}
              </Form.Group>

              <div style={{display: 'flex', justifyContent: 'space-between', marginTop: 16}}>
                <Button variant="success" type="submit" disabled={isSubmitting}>
                  Kommentar posten
                </Button>
              </div>
            </Form>
          )
        }
      </Formik>
    </div>
  )
}
