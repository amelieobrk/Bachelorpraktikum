import React from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';

interface NewTagInput {
    newTagName: string
  }

const NewTagValidationSchema = yup.object().shape({
  newTagName: yup.string().required("Bitte gib einen Namen für den Tag an."),
})

/**
 * Modal to create a new module.
 */
export default function CreateTagModal(props : {moduleId: number, isOpen: boolean, onClose: () => void, onCreated: () => void}) {

  const {moduleId, isOpen, onClose, onCreated} = props;

  return (
    <Modal show={isOpen} onHide={onClose} transition="false">
    <Modal.Header>
      <Modal.Title>Tag Erstellen</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Formik
        initialValues={{
          newTagName: ''
        }}
        onSubmit={(
          values: NewTagInput,
          { setSubmitting, setFieldError }: FormikHelpers<NewTagInput>
        ) => {
          setSubmitting(true);
          api.tag.createTag(values.newTagName, moduleId)
            .then(() => {
              setSubmitting(false)
              onCreated()
            })
            .catch((err) => {
              if (err.response && err.response.data && err.response.data.msg) {
                setFieldError('newTagName', err.response.data.msg);
              } else {
                setFieldError('newTagName', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
              }
              setSubmitting(false);
            });
        }}
        validationSchema={NewTagValidationSchema}
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
              <Form.Group controlId="TagName">

                <Form.Control
                  data-testid="newTagName-input"
                  autoFocus
                  isInvalid={errors.newTagName != null}
                  type="text"
                  value={values.newTagName}
                  name="newTagName"
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Name des Tags"
                />
                {errors.newTagName && (
                  <Form.Control.Feedback type="invalid">{errors.newTagName}</Form.Control.Feedback>
                )}
              </Form.Group>
              <Button
                data-testid="submit-button"
                block
                type="submit"
                disabled={isSubmitting}
                variant="primary"
              >
                Hinzufügen
              </Button>
            </Form>
          )
        }
      </Formik>
    </Modal.Body>
    <Modal.Footer>
      <Button onClick={onClose}>Schließen</Button>
    </Modal.Footer>
  </Modal>
  );
}
