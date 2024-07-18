import React, {useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import * as yup from 'yup';
import api from "../../api";
import {Formik, FormikHelpers} from "formik";
import Notification from "../General/Notification";
import {AxiosError} from "axios";

interface GeneralInformationProps {
  userId: number
  username: string
  firstName: string
  lastName: string
  email: string
  canEditEmail: boolean
  reloadAuth: () => void
}

interface UserSettingsInput {
  username: string
  firstName: string
  lastName: string
  email: string
}

const UserSettingsValidationSchema = yup.object().shape({
  firstName: yup.string()
    .min(3, "Dein Vorname sollte mindestens 3 Zeichen beinhalten.")
    .max(128, "Bitte kürze Deinen Vorname auf 128 Zeichen.")
    .required("Bitte gib Deinen Vornamen ein."),
  lastName: yup.string()
    .min(3, "Dein Nachname sollte mindestens 3 Zeichen beinhalten.")
    .max(128, "Bitte kürze Deinen Nachname auf 128 Zeichen.")
    .required("Bitte gib Deinen Nachname ein."),
  username: yup.string()
    .min(3, "Dein Username sollte mindestens 3 Zeichen beinhalten.")
    .max(64, "Bitte kürze Deinen Username auf 64 Zeichen.")
    .required("Bitte wähle einen Username."),
  email: yup.string()
})

/**
 * Form to update general account information
 */
export default function GeneralInformation(props : GeneralInformationProps) {

  const {userId, username, firstName, lastName, reloadAuth, email, canEditEmail} = props;

  const [saveNotificationOpen, setSaveNotificationOpen] = useState(false);

  return (
    <>
      <Formik
        initialValues={{
          username,
          firstName,
          lastName,
          email
        }}
        onSubmit={(
          values: UserSettingsInput,
          { setSubmitting, setFieldError }: FormikHelpers<UserSettingsInput>
        ) => {

          if (username !== values.username || firstName !== values.firstName || lastName !== values.lastName || email !== values.email) {
            setSubmitting(true);
            api.user.updateUserData(
              userId,
              values.username === username ? null : values.username,
              values.firstName === firstName ? null : values.firstName,
              values.lastName === lastName ? null : values.lastName,
              values.email === email ? null : values.email
            )
              .then(() => {
                setSubmitting(false);
                reloadAuth();
                setSaveNotificationOpen(true);
              })
              .catch((err : AxiosError) => {
                if (err.response && err.response.data && err.response.data.msg) {
                  setFieldError('lastName', err.response.data.msg);
                } else {
                  setFieldError('lastName', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
                }
                setFieldError('username', '');
                setFieldError('firstName', '');
                setSubmitting(false);
              });
          } else {
            setSubmitting(false);
          }

        }}
        validationSchema={UserSettingsValidationSchema}
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
                  data-testid="username-input"
                  isInvalid={errors.username != null}
                  name="username"
                  value={values.username}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Username"
                />
                {errors.username && (
                  <Form.Control.Feedback type="invalid">{errors.username}</Form.Control.Feedback>
                )}

              </Form.Group>

              <Form.Group>
                <Form.Control
                  data-testid="first-name-input"
                  isInvalid={errors.firstName != null}
                  name="firstName"
                  value={values.firstName}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Vorname"
                />
                {errors.firstName && (
                  <Form.Control.Feedback type="invalid">{errors.firstName}</Form.Control.Feedback>
                )}

              </Form.Group>

              <Form.Group>
                <Form.Control
                  data-testid="last-name-input"
                  isInvalid={errors.lastName != null}
                  name="lastName"
                  value={values.lastName}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Nachname"
                />
                {errors.lastName && (
                  <Form.Control.Feedback type="invalid">{errors.lastName}</Form.Control.Feedback>
                )}

              </Form.Group>

              {
                canEditEmail && (
                  <Form.Group>
                    <Form.Control
                      data-testid="email-input"
                      isInvalid={errors.email != null}
                      name="email"
                      value={values.email}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder="Email"
                    />
                    {errors.lastName && (
                      <Form.Control.Feedback type="invalid">{errors.email}</Form.Control.Feedback>
                    )}

                  </Form.Group>
                )
              }

              <Button
                data-testid="save-button"
                type="submit"
                disabled={isSubmitting || values.username === '' || values.firstName === '' || values.lastName === ''}
                variant="primary"
              >
                Speichern
              </Button>
            </Form>
          )
        }
      </Formik>
      <Notification
        onClose={() => setSaveNotificationOpen(false)}
        open={saveNotificationOpen}
        text="Deine Änderungen wurden erfolgreich gespeichert"
        header="Erfolgreich gespeichert"
      />
    </>
  );
}
