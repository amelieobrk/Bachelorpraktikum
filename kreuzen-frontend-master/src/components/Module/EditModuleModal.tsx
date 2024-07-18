import React, {useEffect, useState} from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';
import {University} from "../../api/auth";

interface EditModuleInput {
  newModuleName: string
  newModuleUniversityId: number
  newModuleUniversityWide: boolean
}

const EditModuleValidationSchema = yup.object().shape({
  newModuleName: yup.string().required("Bitte gib einen mnamen an. (Form: WiSe 2020/2021)"),
  newModuleUniversityId: yup.number().min(1).required("Bitte gibt die ID der Universität an"),
})

/**
 * Modal to edit a module.
 */
export default function EditModuleModal(props : {moduleId: number, moduleName: string, universityId: number, universityWide: boolean, isOpen: boolean, onClose: () => void, onEdited: () => void}) {

  const {moduleId, moduleName, universityId, universityWide, isOpen, onClose, onEdited} = props;

  const [universities, setUniversities] = useState<University[]>([]);

  useEffect(() => {
    api.university.getUniversities().then(setUniversities);
  }, [])

  return (
    <Modal show={isOpen} onHide={onClose} transition="false">
      <Modal.Header>
        <Modal.Title>Modul bearbeiten</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Formik
          initialValues={{
            newModuleName: moduleName,
            newModuleUniversityId: universityId,
            newModuleUniversityWide: universityWide
          }}
          onSubmit={(
            values: EditModuleInput,
            { setSubmitting, setFieldError }: FormikHelpers<EditModuleInput>
          ) => {
            setSubmitting(true);
            api.module.updateModule(moduleId, values.newModuleUniversityWide, values.newModuleName, values.newModuleUniversityId)
              .then(() => {
                setSubmitting(false)
                onEdited();
              })
              .catch((err) => {
                if (err.response && err.response.data && err.response.data.msg) {
                  setFieldError('newModuleUniversityWide', err.response.data.msg);
                } else {
                  setFieldError('newModuleUniversityWide', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
                }
                setFieldError('newModuleUniversityWide', '');
                setSubmitting(false);
              });
          }}
          validationSchema={EditModuleValidationSchema}
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
                <Form.Group controlId="newModuleName">

                  <Form.Control
                    data-testid="newModuleName-input"
                    autoFocus
                    isInvalid={errors.newModuleName != null}
                    type="text"
                    value={values.newModuleName}
                    name="newModuleName"
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Bezeichnung des Moduls"
                  />
                  {errors.newModuleName && (
                    <Form.Control.Feedback type="invalid">{errors.newModuleName}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="newModuleUniversityId">
                  <Form.Control
                    data-testid="newModuleUniversityId-input"
                    isInvalid={errors.newModuleUniversityId != null}
                    type="number"
                    name="newModuleUniversityId"
                    value={values.newModuleUniversityId}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="ID der Universität"
                    as="select"
                  >
                    <option value={0}>Universität</option>
                    {
                      universities.map(uni => (
                        <option value={uni.id}>{uni.name}</option>
                      ))
                    }
                  </Form.Control>
                  {errors.newModuleUniversityId && (
                    <Form.Control.Feedback type="invalid">{errors.newModuleUniversityId}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group>
                  <Form.Check
                    name="newModuleUniversityWide"
                    label="Universitätsübergreifend?"
                    onChange={handleChange}
                    id="checkbox"
                    checked={values.newModuleUniversityWide}
                  />
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
