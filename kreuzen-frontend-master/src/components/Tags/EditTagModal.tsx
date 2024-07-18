import React from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';

interface EditTagInput {
  newTagName: string
}

/**
 * Modal to edit a tag.
 */
export default function EditTagModal(props : {tagId: number, tagName: string, isOpen: boolean, onClose: () => void, onEdited: () => void}) {

  const {tagId, tagName, isOpen, onClose, onEdited} = props;

  const EditTagValidationSchema = yup.object().shape({
    newTagName: yup.string().required("Bitte gib einen Namen für den Tag an."),
  })

  return (
    
    <Modal show={isOpen} onHide={onClose} transition="false">
      <Modal.Header>
        <Modal.Title>Tag bearbeiten</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Formik
          initialValues={{
            newTagName: tagName || "",
          }}
          onSubmit={(
            values: EditTagInput,
            { setSubmitting, setFieldError }: FormikHelpers<EditTagInput>
          ) => {
            setSubmitting(true);
            api.tag.updateTag(tagId, values.newTagName)
              .then(() => {
                setSubmitting(false)
                onEdited();
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
          validationSchema={EditTagValidationSchema}
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
                <Form.Group controlId="newTagName">

                  <Form.Control
                    data-testid="newTagName-input"
                    autoFocus
                    isInvalid={errors.newTagName != null}
                    type="text"
                    value={values.newTagName}
                    name="newTagName"
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Bezeichnung des Tags"
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
                  Änderungen bestätigen
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
