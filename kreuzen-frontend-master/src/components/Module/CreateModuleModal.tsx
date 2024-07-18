import React, {useEffect, useState} from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import api from '../../api';
import { Formik, FormikHelpers } from 'formik';
import * as yup from 'yup';
import {University} from "../../api/auth";

interface ModuleInput {
  name: string
  universityId: number
  universityWide: boolean
}

const moduleValidationSchema = yup.object().shape({
  name: yup.string().required("Bitte gib einen Modulnamen an. (Form: WiSe 2020/2021)"),
  universityId: yup.number().min(1).required("Bitte gibt die ID der Universität an"),

})

/**
 * Modal to create a new module.
 */
export default function CreateModuleModal(props : {isOpen: boolean, onClose: () => void, onCreated: () => void}) {

  const {isOpen, onClose, onCreated} = props;

  const [universities, setUniversities] = useState<University[]>([]);

  useEffect(() => {
    api.university.getUniversities().then(setUniversities);
  }, [])

  return (
    <Modal show={isOpen} onHide={onClose} transition="false">
      <Modal.Header>
        <Modal.Title>Modul hinzufügen</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Formik
          initialValues={{
            name: '',
            universityId: 0,
            universityWide: false
          }}
          onSubmit={(
            values: ModuleInput,
            { setSubmitting, setFieldError }: FormikHelpers<ModuleInput>
          ) => {
            setSubmitting(true);
            api.module.createModule(values.universityWide, values.name, values.universityId)
              .then(() => {
                setSubmitting(false)
                onCreated();
              })
              .catch((err) => {
                if (err.response && err.response.data && err.response.data.msg) {
                  setFieldError('universityWide', err.response.data.msg);
                } else {
                  setFieldError('universityWide', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
                }
                setFieldError('name', '');
                setFieldError('universityId', '');
                setSubmitting(false);
              });
          }}
          validationSchema={moduleValidationSchema}
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
                <Form.Group controlId="name">

                  <Form.Control
                    data-testid="name-input"
                    autoFocus
                    isInvalid={errors.name != null}
                    type="text"
                    value={values.name}
                    name="name"
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Bezeichnung des Moduls (Beispiel: WiSe20/21)"
                  />
                  {errors.name && (
                    <Form.Control.Feedback type="invalid">{errors.name}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group controlId="universityId">
                  <Form.Control
                    data-testid="universityId-input"
                    isInvalid={errors.universityId != null}
                    as="select"
                    name="universityId"
                    value={values.universityId}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="ID der Universität"
                  >
                    <option value={0}>Universität</option>
                    {
                      universities.map(uni => (
                        <option value={uni.id}>{uni.name}</option>
                      ))
                    }
                  </Form.Control>
                  {errors.universityId && (
                    <Form.Control.Feedback type="invalid">{errors.universityId}</Form.Control.Feedback>
                  )}
                </Form.Group>

                <Form.Group>
                  <Form.Check
                    name="universityWide"
                    label="Universitätsübergreifend?"
                    onChange={handleChange}
                    id="checkbox"
                    checked={values.universityWide}
                  />
                </Form.Group>

                <Button
                  data-testid="submit-button"
                  block
                  type="submit"
                  disabled={isSubmitting}
                  variant="primary"
                >
                  Erstellen
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
