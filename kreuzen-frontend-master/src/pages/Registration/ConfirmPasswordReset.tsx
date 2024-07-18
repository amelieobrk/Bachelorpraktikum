import React from 'react';
import {
  Button, Form, Col, Row, Card,
} from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useHistory } from 'react-router-dom'
import api from "../../api";
import * as yup from 'yup'
import { Formik, FormikHelpers } from "formik";
import CardHeader from "../../components/General/CardHeader";

interface ConfirmPasswordResetInput {
  token: string
  newPassword: string
  newPasswordRepeat: string
}

const ConfirmPasswordResetConfirmationSchema = yup.object().shape({
  token: yup.string().required("Der Reset Code muss angegeben werden."),
  newPassword: yup.string()
    .min(8, "Das Passwort ist zu schwach. Es sollte mindestens 8 Zeichen haben.")
    .max(256, "Das Passwort sollte maximal 256 Zeichen haben.")
    .required("Ein Passwort ist notwendig."),
  newPasswordRepeat: yup.string()
    .required("Bitte gib dein Passwort zur Bestätigung erneut ein.")
    .oneOf([yup.ref('newPassword'), null], 'Die Passwörter müssen übereinstimmen.')
})

/**
 * The request password reset page allows the user to set a new password using a reset token.
 */
export default function RequestPasswordReset() {

  const history = useHistory();
  const urlToken: string | null = new URLSearchParams(history.location.search).get('t');

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Passwort vergessen" secondary />

        <Formik
          initialValues={{
            token: urlToken || '',
            newPassword: '',
            newPasswordRepeat: ''
          }}
          onSubmit={(
            values: ConfirmPasswordResetInput,
            { setSubmitting, setFieldError }: FormikHelpers<ConfirmPasswordResetInput>
          ) => {
            setSubmitting(true);
            api.auth.confirmPasswordReset(values.token, values.newPassword).then(() => {
              setSubmitting(false);
              // Redirect to start page.
              history.push("/");
            }).catch(e => {
              setFieldError("token", "");
              setFieldError("newPassword", "");
              setFieldError("newPasswordRepeat", e?.response?.data?.msg || "Ein Fehler ist aufgetreten. Bitte versuche es später erneut.");
              setSubmitting(false);
            });
          }}
          validationSchema={ConfirmPasswordResetConfirmationSchema}
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
                    data-testid="token"
                    name="token"
                    value={values.token}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Code"
                    isInvalid={errors.token != null}
                  />
                  {
                    errors.token && (
                      <Form.Control.Feedback type="invalid">
                        {errors.token}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>
                <Form.Group>
                  <Form.Control
                    autoFocus
                    data-testid="new-password-input"
                    type="password"
                    name="newPassword"
                    value={values.newPassword}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Neues Passwort"
                    isInvalid={errors.newPassword != null}
                  />
                  {
                    errors.newPassword && (
                      <Form.Control.Feedback type="invalid">
                        {errors.newPassword}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>
                <Form.Group>
                  <Form.Control
                    data-testid="new-password-repeat-input"
                    type="password"
                    name="newPasswordRepeat"
                    value={values.newPasswordRepeat}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Neues Passwort wiederholen"
                    isInvalid={errors.newPasswordRepeat != null}
                  />
                  {
                    errors.newPasswordRepeat && (
                      <Form.Control.Feedback type="invalid">
                        {errors.newPasswordRepeat}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>

                <Button block type="submit" disabled={isSubmitting} data-testid="submit-button" variant="primary">
                  Passwort zurücksetzen
                </Button>
              </Form>
            )
          }
        </Formik>

        <div style={{ marginTop: 16, padding: 8 }}>
          <Row>
            <Col>
              <Button size="sm" variant="secondary" block as={Link} to="/">
                Login
              </Button>
            </Col>
            <Col>
              <Button size="sm" variant="secondary" block as={Link} to="/register">
                Registrieren
              </Button>
            </Col>
          </Row>
        </div>

      </Card.Body>
    </Card>
  );
}
