import React from 'react';
import {
  Button, Col, Form, Row, Card
} from 'react-bootstrap';
import { Link } from 'react-router-dom';
import api from '../../api';
import { Formik, FormikHelpers } from "formik";
import * as yup from 'yup';
import logo from '../../Grafiken/Logo.png';

interface LoginInput {
  username: string
  password: string
}

const LoginValidationSchema = yup.object().shape({
  username: yup.string().required("Bitte gib Deinen Nutzernamen oder Deine Email Adresse ein."),
  password: yup.string().required("Bitte gib Dein Passwort ein."),
})

/**
 * Login component for the user to authenticate.
 */
export default function HomeLogin() {

  return (
    <>
      <Row>
        <Col>

        </Col>
        <Col>
          <div style={{ width: 'auto', height: 'auto', marginBottom: 32 }}>
            <img src={logo} alt="Kreuzen Logo" width="300" height="300" />
          </div>
        </Col>
        <Col>

        </Col>
      </Row>
      <Card>
        <Card.Body>
          <Formik
            initialValues={{
              username: '',
              password: ''
            }}
            onSubmit={(
              values: LoginInput,
              { setSubmitting, setFieldError }: FormikHelpers<LoginInput>
            ) => {
              setSubmitting(true);
              api.auth.login(values.username, values.password)
                .then(() => {
                  setSubmitting(false)
                })
                .catch((err) => {
                  if (err.response && err.response.data && err.response.data.msg) {
                    setFieldError('password', err.response.data.msg);
                  } else if (err.response && err.response.status === 401) {
                    setFieldError('password', 'Nutzername und Passwort stimmen nicht überein.');
                  } else {
                    setFieldError('password', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
                  }
                  setFieldError('username', '');
                  setSubmitting(false);
                });
            }}
            validationSchema={LoginValidationSchema}
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
                  <Form.Group controlId="email">
                    <Form.Control
                      data-testid="username-input"
                      autoFocus
                      isInvalid={errors.username != null}
                      type="text"
                      value={values.username}
                      name="username"
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder="Benutzername oder Email"
                    />
                    {errors.username && (
                      <Form.Control.Feedback type="invalid">{errors.username}</Form.Control.Feedback>
                    )}
                  </Form.Group>
                  <Form.Group controlId="password">
                    <Form.Control
                      data-testid="password-input"
                      isInvalid={errors.password != null}
                      type="password"
                      name="password"
                      value={values.password}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      placeholder="Passwort"
                    />
                    {errors.password && (
                      <Form.Control.Feedback type="invalid">{errors.password}</Form.Control.Feedback>
                    )}

                  </Form.Group>

                  <Button
                    data-testid="submit-button"
                    block
                    type="submit"
                    disabled={isSubmitting}
                    variant="primary"
                  >
                    Login
                </Button>
                </Form>
              )
            }
          </Formik>

          <div style={{ marginTop: 8, marginRight: "auto" }}>
            <Button
              size="sm"
              variant="link"
              as={Link}
              to="/request-password-reset"
            >
              Passwort vergessen?
          </Button>
          </div>
          <div style={{ marginTop: 16, padding: 8 }}>
            <Row>
              <Col>
                <Button size="sm" variant="secondary" block as={Link} to="/register">
                  Registrieren
                </Button>
              </Col>
              <Col>
                <Button size="sm" variant="secondary" block as={Link} to="/confirm-email">
                  Account freischalten
                </Button>
              </Col>
            </Row>
          </div>
        </Card.Body>
      </Card>
    </>
  );
}
