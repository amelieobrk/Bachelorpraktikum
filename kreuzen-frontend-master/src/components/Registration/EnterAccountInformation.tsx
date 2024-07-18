import React from "react";
import { Button, Card, Col, Form, Row } from "react-bootstrap";
import { Link } from "react-router-dom";
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup'
import CardHeader from "../General/CardHeader";

interface BasicInformationInput {
  firstName: string
  lastName: string
  username: string
  email: string
  password: string
  passwordRepeat: string
}

const BasicInformationSchema = yup.object().shape({
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
    .min(3, "Eine Email Adresse sollte mindestens 3 Zeichen beinhalten.")
    .required("Bitte gib Deine studentische Email Adresse ein."),
  password: yup.string()
    .min(8, "Das Passwort ist zu schwach. Es sollte mindestens 8 Zeichen haben.")
    .max(256, "Das Passwort sollte maximal 256 Zeichen haben.")
    .required("Ein Passwort ist notwendig."),
  passwordRepeat: yup.string()
    .required("Bitte gib Dein Passwort zur Bestätigung erneut ein.")
    .oneOf([yup.ref('password'), null], 'Die Passwörter müssen übereinstimmen.')
})

interface BasicInformationProps {
  firstName: string
  lastName: string
  username: string
  email: string
  password: string
  onNext: (firstName: string, lastName: string, username: string, email: string, password: string) => void
  hidden: boolean
  error: string | null
}

/**
 * Component to enter basic account information like name, email and password.
 * @param props
 */
const EnterBasicInformation = (props: BasicInformationProps) => {

  return (
    <Card hidden={props.hidden}>
      <Card.Body>
        <CardHeader text="Registrierung" secondary />
        <Formik
          initialValues={{
            firstName: props.firstName || '',
            lastName: props.lastName || '',
            username: props.username || '',
            email: props.email || '',
            password: props.password || '',
            passwordRepeat: props.password || ''
          }}
          onSubmit={(
            values: BasicInformationInput,
            { setSubmitting }: FormikHelpers<BasicInformationInput>
          ) => {
            props.onNext(values.firstName, values.lastName, values.username, values.email, values.password);
            setSubmitting(false);
          }}
          validationSchema={BasicInformationSchema}
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
                <Form.Group controlId="vorname">
                  <Form.Control
                    data-testid="firstName-input"
                    name="firstName"
                    value={values.firstName}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Vorname"
                    isInvalid={errors.firstName != null || props.error != null}
                  />
                  {
                    errors.firstName && (
                      <Form.Control.Feedback type="invalid">
                        {errors.firstName}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>
                <Form.Group controlId="nachname">
                  <Form.Control
                    data-testid="lastName-input"
                    name="lastName"
                    value={values.lastName}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Nachname"
                    isInvalid={errors.lastName != null || props.error != null}
                  />
                  {
                    errors.lastName && (
                      <Form.Control.Feedback type="invalid">
                        {errors.lastName}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>
                <Form.Group controlId="email">
                  <Form.Control
                    data-testid="email-input"
                    type="email"
                    name="email"
                    value={values.email}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Studentische E-Mail Adresse"
                    isInvalid={errors.email != null || props.error != null}
                  />
                  {
                    errors.email && (
                      <Form.Control.Feedback type="invalid">
                        {errors.email}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>
                <Form.Group controlId="benutzername">
                  <Form.Control
                    data-testid="username-input"
                    name="username"
                    value={values.username}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Benutzername"
                    isInvalid={errors.username != null || props.error != null}
                  />
                  {
                    errors.username && (
                      <Form.Control.Feedback type="invalid">
                        {errors.username}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>
                <Form.Group controlId="password">
                  <Form.Control
                    data-testid="password-input"
                    type="password"
                    name="password"
                    value={values.password}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Passwort"
                    isInvalid={errors.password != null || props.error != null}
                  />
                  {
                    errors.password && (
                      <Form.Control.Feedback type="invalid">
                        {errors.password}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>
                <Form.Group controlId="passwordCheck">
                  <Form.Control
                    data-testid="password-repeat-input"
                    type="password"
                    name="passwordRepeat"
                    value={values.passwordRepeat}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Passwort wiederholen"
                    isInvalid={errors.passwordRepeat != null || props.error != null}
                  />
                  {
                    (errors.passwordRepeat || props.error) && (
                      <Form.Control.Feedback type="invalid">
                        {errors.passwordRepeat || props.error}
                      </Form.Control.Feedback>
                    )
                  }
                </Form.Group>

                <div style={{ marginTop: 16, padding: 8 }}>
                  <Row>
                    <Col>
                      <Button size="sm" variant="secondary" block as={Link} to="/">
                        Login
                      </Button>
                    </Col>
                    <Col>
                      <Button
                        data-testid="next-button"
                        size="sm"
                        variant="primary"
                        block
                        type="submit"
                        disabled={isSubmitting}
                      >
                        Weiter
                      </Button>
                    </Col>
                  </Row>
                </div>
              </Form>
            )}
        </Formik>
      </Card.Body>
    </Card>
  )
};

export default EnterBasicInformation;