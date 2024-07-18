import React from 'react';
import {
  Button, Col, Form, Row, Card
} from 'react-bootstrap';
import { Link } from 'react-router-dom';
import api from "../../api";
import { useHistory } from 'react-router-dom';
import { Formik, FormikHelpers } from "formik";
import * as yup from "yup";
import CardHeader from "../../components/General/CardHeader";

interface ConfirmEmailInput {
  token: string
}

const ConfirmEmailSchema = yup.object().shape({
  token: yup.string().required("Bitte gib Deinen Code ein."),
})

/**
 * Page used to confirm an account to gain access to the platform.
 */
export default function ConfirmEmail() {

  const history = useHistory();
  const urlToken: string | null = new URLSearchParams(history.location.search).get('t');

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Account freischalten" secondary />

        <p>
          Falls der Bestätigungslink nicht funktioniert, kannst du hier den Code eingeben.
        </p>
        <p>
          Einfach den Code kopieren und hier ins Feld eingeben.
        </p>

        <Formik
          initialValues={{
            token: urlToken || ''
          }}
          onSubmit={(
            values: ConfirmEmailInput,
            { setSubmitting, setFieldError }: FormikHelpers<ConfirmEmailInput>
          ) => {
            setSubmitting(true);
            api.auth.confirmAccount(values.token).then(() => {
              setSubmitting(false);
              // Automatic login should happen...
              history.push('/');
            }).catch(e => {
              setSubmitting(false);
              if (e.response?.data?.msg) {
                setFieldError('token', e.response.data.msg);
              } else {
                setFieldError('token', 'Ein Fehler ist aufgetreten. Bitte versuche es später erneut.');
              }
            })
          }}
          validationSchema={ConfirmEmailSchema}
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
                <Form.Group controlId="aktivierungscode">
                  <Form.Control
                    data-testid="code-input"
                    autoFocus
                    value={values.token}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    name="token"
                    placeholder="Aktivierungscode"
                    isInvalid={errors.token != null}
                  />
                  {errors.token && (
                    <Form.Control.Feedback type="invalid">
                      {errors.token}
                    </Form.Control.Feedback>
                  )}
                </Form.Group>

                <Button block type="submit" variant="primary" disabled={isSubmitting} data-testid="submit-button">
                  Aktivieren
                </Button>
              </Form>
            )}
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
