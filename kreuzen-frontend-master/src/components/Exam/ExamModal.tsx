import React, {useCallback, useEffect, useState} from 'react';
import { Button, Modal } from 'react-bootstrap';
import api from '../../api';
import {Exam} from "../../api/exam";
import {prettyPrintDate} from "../../utils";
import {Course} from "../../api/course";
import PasswordConfirmation from "../General/PasswordConfirmation";
import Notification from "../General/Notification";
import EditExam from "./EditExam";

/**
 * Modal to display an exam.
 */
export default function ExamModal(props : {
  examId: number | null
  isOpen: boolean
  onClose: () => void
  onChanged?: () => void
}) {

  const {examId, isOpen, onClose, onChanged} = props;

  const [deleteConfirmationOpen, setDeleteConfirmationOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [exam, setExam] = useState<Exam | null>(null);
  const [course, setCourse] = useState<Course | null>(null);

  const handleClose = () => {
    setEditOpen(false);
    setDeleteOpen(false);
    onClose();
  }

  const loadExam = useCallback(() => {
    if (examId) {
      api.exam.getExam(examId).then(e => {
        setExam(e)
        api.course.getCourse(e.courseId).then(setCourse)
      });
    }
  }, [examId]);

  useEffect(() => {
    if (examId) {
      loadExam();
    } else {
      setExam(null);
    }
  }, [examId, loadExam])

  const onDelete = (id: number, password: string, setSubmitting: (isSubmitting: boolean) => void, setFieldError: (field: string, message: string | undefined) => void): void => {
    api.exam.deleteExam(id, password)
      .then(() => {
        setDeleteConfirmationOpen(true);
        if (onChanged) {
          onChanged();
        }
        handleClose();
        setSubmitting(false);
      })
      .catch((err) => {
        if (err.response && err.response.data && err.response.data.msg) {
          setFieldError('password', err.response.data.msg);
        } else {
          setFieldError('password', 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
        }
        setSubmitting(false);
      });
  }

  return (
    <>
      {
        examId && (
          <Modal show={isOpen} onHide={handleClose} transition="false">
            <Modal.Header>
              <Modal.Title>Klausur</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {
                deleteOpen ? (
                  <>
                    <p>
                      Bist Du sicher, dass Du diese Klausur löschen möchtest?<br/>
                      Alle Fragen aus dieser Klausur werden unwiederruflich gelöscht!
                    </p>
                    <PasswordConfirmation id={examId} onDelete={onDelete} />
                  </>
                ) :  exam ? (
                  editOpen ? (
                      <EditExam
                        id={examId}
                        name={exam.name}
                        date={exam.date}
                        isComplete={exam?.isComplete}
                        isRetry={exam?.isRetry}
                        onUpdated={() => {
                          loadExam();
                          setEditOpen(false);
                          if (onChanged) onChanged();
                        }}
                      />
                    ) : (
                      <>
                        <b>ID:</b> {exam.id} <br/>
                        <b>Name:</b> {exam.name} <br/>
                        <b>Datum:</b> {prettyPrintDate(exam.date)} <br/>
                        <b>Kurs ID:</b> {exam.courseId} <br/>
                        <b>Kurs Name:</b> {course?.name || "Lädt..."} <br/>
                        <b>Wiederholung:</b> {exam.isRetry ? 'Ja' : 'Nein'} <br/>
                        <b>Vollständig:</b> {exam.isComplete ? 'Ja' : 'Nein'} <br/>
                      </>
                    )
                ) : (
                  "Klausur wird geladen..."
                )
              }
            </Modal.Body>
            <Modal.Footer>
              {
                editOpen || deleteOpen ? (
                  <Button
                    className="mr-auto"
                    onClick={() => {
                      setDeleteOpen(false);
                      setEditOpen(false);
                    }}
                  >
                    Abbrechen
                  </Button>
                ) : (
                  <>
                    <Button variant="danger" onClick={() => setDeleteOpen(true)}>Löschen</Button>
                    <Button variant="secondary" onClick={() => setEditOpen(true)} className="mr-auto">Bearbeiten</Button>
                  </>
                )
              }
              <Button onClick={handleClose}>Schließen</Button>
            </Modal.Footer>
          </Modal>
        )
      }
      <Notification
        onClose={() => setDeleteConfirmationOpen(false)}
        open={deleteConfirmationOpen}
        text="Die Klausur wurde gelöscht."
        header="Klausur gelöscht"
      />
    </>
  );
}
