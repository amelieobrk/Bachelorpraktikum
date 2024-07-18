import React, {useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import * as yup from 'yup';
import api from "../../api";
import {Formik, FormikHelpers} from "formik";
import Notification from "../General/Notification";
import {AxiosError} from "axios";

interface AdminUpdatePasswordProps {
  userId: number
}

interface AdminChangePasswordInput {
  newPassword: string
  newPasswordRepeat: string
}

const AdminChangePasswordValidationSchema = yup.object().shape({
  newPassword: yup.string()
    .min(8, "Das neues Passwort ist zu schwach. Es sollte mindestens 8 Zeichen haben.")
    .max(256, "Das neues Passwort sollte maximal 256 Zeichen haben.")
    .required("Ein Passwort ist notwendig."),
  newPasswordRepeat: yup.string()
    .required("Bitte gib das neues Passwort zur Bestätigung erneut ein.")
    .oneOf([yup.ref('newPassword'), null], 'Die neuen Passwörter müssen übereinstimmen.')
})

/**
 * Form to update password
 */
export default function AdminUpdatePassword(props : AdminUpdatePasswordProps) {

  const {userId} = props;

  const [saveNotificationOpen, setSaveNotificationOpen] = useState(false);

  return (
    <>
      <Formik
        initialValues={{
          newPassword: '',
          newPasswordRepeat: ''
        }}
        onSubmit={(
          values: AdminChangePasswordInput,
          { setSubmitting, setFieldError, resetForm }: FormikHelpers<AdminChangePasswordInput>
        ) => {
          setSubmitting(true);
          api.user.updatePassword(userId, values.newPassword, null)
            .then(() => {
              setSubmitting(false)
              resetForm();
              setSaveNotificationOpen(true)
            })
            .catch((err : AxiosError) => {
              if (err.response && err.response.data && err.response.data.msg) {
                setFieldError('newPasswordRepeat', err.response.data.msg);
              } else {
                setFieldError('newPasswordRepeat', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
              }
              setFieldError('newPassword', '');
              setSubmitting(false);
            });
        }}
        validationSchema={AdminChangePasswordValidationSchema}
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
              <Form.Group>
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

              <Form.Group>
                <Form.Control
                  data-testid="new-password-repeat-input"
                  isInvalid={errors.newPasswordRepeat != null}
                  type="password"
                  name="newPasswordRepeat"
                  value={values.newPasswordRepeat}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Neues Passwort wiederholt"
                />
                {errors.newPasswordRepeat && (
                  <Form.Control.Feedback type="invalid">{errors.newPasswordRepeat}</Form.Control.Feedback>
                )}

              </Form.Group>

              <Button
                data-testid="submit-password-button"
                type="submit"
                disabled={isSubmitting || values.newPassword === '' || values.newPasswordRepeat === ''}
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
        text="Das Passwort wurde geupdated. Bitte gib dem Nutzer bescheid."
        header="Passwort geändert"
      />
    </>
  );
}
