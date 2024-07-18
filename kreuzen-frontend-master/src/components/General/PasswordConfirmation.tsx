import { Formik, FormikHelpers } from 'formik';
import React from 'react';
import { Button, Form } from 'react-bootstrap';
import * as yup from 'yup';

interface PasswordConfirmationProps {
  id: number
  onDelete: (id: number, password: string, setSubmitting: (isSubmitting: boolean) => void, setFieldError: (field: string, message: string | undefined) => void) => void
  confirmationText?: string
}

interface PasswordInput {
  password: string
}

const PasswordValidationSchema = yup.object().shape({
  password: yup.string().required("Bitte gib dein Passwort zur Bestätigung ein")
})

/**
 * Confirmation of password for delete
 *
 * @param props
 */
export default function PasswordConfirmation(props: PasswordConfirmationProps) {

  return (
    <Formik
      initialValues={{
        password: '',
      }}
      onSubmit={
        (values: PasswordInput, { setSubmitting, setFieldError }: FormikHelpers<PasswordInput>) => {
          return props.onDelete(props.id, values.password, setSubmitting, setFieldError)
        }
      }
      validationSchema={PasswordValidationSchema}
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
                data-testid="password-input"
                isInvalid={errors.password != null}
                autoFocus
                type="password"
                value={values.password}
                name="password"
                onChange={handleChange}
                onBlur={handleBlur}
                placeholder="Passwort zur Bestätigung eingeben."
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
              {props.confirmationText || "Bestätigen"}
            </Button>
          </Form>
        )
      }
    </Formik>
  )
}