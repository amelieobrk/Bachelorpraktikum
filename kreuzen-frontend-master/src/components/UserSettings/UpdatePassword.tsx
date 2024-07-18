import React, { useState } from 'react';
import { Button, Form } from 'react-bootstrap';
import * as yup from 'yup';
import api from "../../api";
import { Formik, FormikHelpers } from "formik";
import Notification from "../General/Notification";

interface UpdatePasswordProps {
  userId: number
}

interface ChangePasswordInput {
  oldPassword: string
  newPassword: string
  newPasswordRepeat: string
}

const ChangePasswordValidationSchema = yup.object().shape({
  oldPassword: yup.string().required("Bitte gib Dein altes Passwort ein."),
  newPassword: yup.string()
    .min(8, "Dein neues Passwort ist zu schwach. Es sollte mindestens 8 Zeichen haben.")
    .max(256, "Dein neues Passwort sollte maximal 256 Zeichen haben.")
    .required("Ein Passwort ist notwendig."),
  newPasswordRepeat: yup.string()
    .required("Bitte gib Dein neues Passwort zur Bestätigung erneut ein.")
    .oneOf([yup.ref('newPassword'), null], 'Die neuen Passwörter müssen übereinstimmen.')
})

/**
 * Form to update password
 */
export default function UpdatePassword(props: UpdatePasswordProps) {

  const { userId } = props;

  const [saveNotificationOpen, setSaveNotificationOpen] = useState(false);

  return (
    <>
      <Formik
        initialValues={{
          oldPassword: '',
          newPassword: '',
          newPasswordRepeat: ''
        }}
        onSubmit={(
          values: ChangePasswordInput,
          { setSubmitting, setFieldError, resetForm }: FormikHelpers<ChangePasswordInput>
        ) => {
          setSubmitting(true);
          api.user.updatePassword(userId, values.newPassword, values.oldPassword)
            .then(() => {
              setSubmitting(false)
              resetForm();
              setSaveNotificationOpen(true)
            })
            .catch((err) => {
              if (err.response && err.response.data && err.response.data.msg) {
                setFieldError('newPasswordRepeat', err.response.data.msg);
              } else {
                setFieldError('newPasswordRepeat', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
              }
              setFieldError('newPassword', '');
              setFieldError('oldPassword', '');
              setSubmitting(false);
            });
        }}
        validationSchema={ChangePasswordValidationSchema}
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
              <Form.Group controlId="password">
                <Form.Control
                  data-testid="old-password-input"
                  isInvalid={errors.oldPassword != null}
                  type="password"
                  name="oldPassword"
                  value={values.oldPassword}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Altes Passwort"
                />
                {errors.oldPassword && (
                  <Form.Control.Feedback type="invalid">{errors.oldPassword}</Form.Control.Feedback>
                )}

              </Form.Group>

              <Form.Group controlId="password">
                <Form.Control
                  data-testid="new-password-input"
                  isInvalid={errors.newPassword != null}
                  type="password"
                  name="newPassword"
                  value={values.newPassword}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Neues Passwort"
                />
                {errors.newPassword && (
                  <Form.Control.Feedback type="invalid">{errors.newPassword}</Form.Control.Feedback>
                )}

              </Form.Group>

              <Form.Group controlId="password">
                <Form.Control
                  data-testid="new-password-repeat-input"
                  isInvalid={errors.newPasswordRepeat != null}
                  type="password"
                  name="newPasswordRepeat"
                  value={values.newPasswordRepeat}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Neues Passwort wiederholen"
                />
                {errors.newPasswordRepeat && (
                  <Form.Control.Feedback type="invalid">{errors.newPasswordRepeat}</Form.Control.Feedback>
                )}

              </Form.Group>

              <Button
                data-testid="submit-password-button"
                type="submit"
                disabled={isSubmitting || values.oldPassword === '' || values.newPassword === '' || values.newPasswordRepeat === ''}
                variant="primary"
              >
                Neues Passwort setzen
              </Button>
            </Form>
          )
        }
      </Formik>
      <Notification
        onClose={() => setSaveNotificationOpen(false)}
        open={saveNotificationOpen}
        text="Dein Passwort wurde geändert."
        header="Passwort geändert"
      />
    </>
  );
}
