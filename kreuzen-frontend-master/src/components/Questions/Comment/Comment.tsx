import React, { useEffect, useState } from 'react';
import {Button, Form} from "react-bootstrap";
import {Comment} from "../../../api/question";
import {prettyPrintDate} from "../../../utils";
import {UserBase} from "../../../api/user";
import api from "../../../api";
import {Formik, FormikHelpers} from "formik";
import {AxiosError} from "axios";
import * as yup from "yup";

interface CommentInput {
  text: string
}

const CommentValidationSchema = yup.object().shape({
  text: yup.string().required("Wenn Du Dein Kommentar entfernen willst, kannst Du den 'Löschen'-Button klicken.")
})

/**
 * Single Comment.
 */
export default function CommentDisplay(props : {
  comment: Comment
  userId: number
  onUpdated: () => void
}) {

  const {comment, userId, onUpdated} = props;
  const creatorId = comment.creatorId;
  const userIsCreator = comment.creatorId === userId;

  const [user, setUser] = useState<UserBase | null>(null)
  const [isEditing, setIsEditing] = useState(false)

  useEffect(() => {
    api.user.getUserById(creatorId).then(setUser)
  }, [creatorId])

  const handleDelete = () => {
    api.question.deleteComment(comment.id).then(onUpdated)
  }

  return (
    <>
      <div style={{display: 'flex', justifyContent: 'space-between'}}>
        <div>
          <b>
            {user ? user.username : 'Loading...'}
          </b>
        </div>
        <div>
          <span className="text-secondary text-sm-right">
            Geschrieben am {prettyPrintDate(comment.createdAt)}
            {
              comment.createdAt.getTime() !== comment.updatedAt.getTime() && (
                <>
                  <br/>
                  Bearbeitet am {prettyPrintDate(comment.updatedAt)}
                </>
              )
            }
          </span>
        </div>
      </div>
      <div style={{marginTop: 16}}>

        {
          isEditing ? (
            <Formik
              initialValues={{
                text: comment.text
              }}
              onSubmit={(
                values: CommentInput,
                { setSubmitting, setFieldError }: FormikHelpers<CommentInput>
              ) => {
                setSubmitting(true);
                api.question.updateComment(
                  comment.id,
                  values.text
                ).then(() => {
                  setSubmitting(false);
                  onUpdated();
                  setIsEditing(false);
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

                    <div style={{display: 'flex', justifyContent: 'flex-start', marginTop: 16}}>
                      <Button variant="success" type="submit" disabled={isSubmitting}>
                        Änderung speichern
                      </Button>
                      <Button
                        style={{marginLeft: 8}}
                        variant="secondary"
                        type="button"
                        disabled={isSubmitting}
                        onClick={() => setIsEditing(false)}
                      >
                        Abbrechen
                      </Button>
                    </div>
                  </Form>
                )
              }
            </Formik>
          ) : (
            <pre className="text-body" style={{fontFamily: 'inherit', fontSize: 'inherit', whiteSpace: 'pre-wrap'}}>
              {comment.text}
            </pre>
          )
        }

      </div>
      {
        userIsCreator && !isEditing && (
          <div style={{display: 'flex', justifyContent: 'flex-end'}}>
            <Button size="sm" onClick={() => setIsEditing(true)}>
              Bearbeiten
            </Button>
            <Button size="sm" variant="danger" onClick={handleDelete} style={{marginLeft: 8}}>
              Löschen
            </Button>
          </div>
        )
      }
    </>
  )
}
