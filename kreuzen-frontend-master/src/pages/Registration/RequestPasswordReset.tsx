import React, { useState } from 'react';
import {
  Button, Form, Col, Row, Card,
} from 'react-bootstrap';
import { Link } from 'react-router-dom';
import api from "../../api";
import { Formik, FormikHelpers } from "formik";
import * as yup from "yup";
import CardHeader from "../../components/General/CardHeader";

interface RequestPasswordResetInput {
  email: string
}

const RequestPasswordResetSchema = yup.object().shape({
  email: yup.string().required("Bitte gib Deine Email Adresse ein."),
})

/**
 * Page used to request a password reset by entering a email address.
 */
export default function RequestPasswordReset() {
  const [success, setSuccess] = useState<boolean>(false);

  return (
    <div>
      <Card>
        <Card.Body>
          <CardHeader text="Passwort vergessen" secondary />

          {
            success ? (
              <>
                <p>
                  Wir haben Dir eine Email geschickt, mit einem Code, um den Passwort Reset zu bestätigen.
                </p>
                <Button block variant="primary" as={Link} to="/confirm-password-reset">
                  Passwort Reset Bestätigen
                </Button>
              </>
            ) : (
                <Formik
                  initialValues={{
                    email: ''
                  }}
                  onSubmit={(
                    values: RequestPasswordResetInput,
                    { setSubmitting, setFieldError }: FormikHelpers<RequestPasswordResetInput>
                  ) => {
                    setSubmitting(true);
                    api.auth.requestPasswordReset(values.email).then(() => {
                      setSubmitting(false);
                      setSuccess(true);
                    }).catch((e) => {
                      setSubmitting(false);
                      if (e?.response?.data?.msg) {
                        setFieldError('email', e.response.data.msg);
                      } else {
                        setFieldError('email', "Ein Fehler ist aufgetreten. Bitte versuche es später erneut.");
                      }
                    })
                  }}
                  validationSchema={RequestPasswordResetSchema}
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
                            data-testid="email-input"
                            autoFocus
                            value={values.email}
                            onChange={handleChange}
                            onBlur={handleBlur}
                            name="email"
                            placeholder="Email Adresse"
                            isInvalid={errors.email != null}
                          />
                          {errors.email && (
                            <Form.Control.Feedback type="invalid">
                              {errors.email}
                            </Form.Control.Feedback>
                          )}
                        </Form.Group>

                        <Button block type="submit" variant="primary" data-testid="submit-button" disabled={isSubmitting}>
                          Passwort Reset Anfragen
                        </Button>
                      </Form>
                    )}
                </Formik>
              )
          }

          <div style={{ marginTop: 16, padding: 8 }}>
            <Row>
              <Col>
                <Button size="sm" variant="secondary" block as={Link} to="/register">
                  Registrieren
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

    </div>
  );
}
