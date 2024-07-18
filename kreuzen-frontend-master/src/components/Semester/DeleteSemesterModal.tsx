import React from 'react';
import api from '../../api';
import PasswordConfirmationModal from "../General/PasswordConfirmationModal"

/**
 * Modal to delete a semester.
 */
export default function DeleteSemesterModal(props : {semesterId: number, isOpen: boolean, onClose: () => void, onDeleted: () => void}) {

  const {isOpen, onClose, onDeleted, semesterId} = props;

  // Deletion process (Correct Password has been provided)
  function onDelete(id: number, password: string, setSubmitting: (isSubmitting: boolean) => void, setFieldError: (field: string, message: string | undefined) => void): void {
    api.semester.deleteSemester(id, password)
      .then(() => {
        onDeleted();
      })
      .catch((err) => {
        if (err.response && err.response.data && err.response.data.msg) {
          setFieldError('password', err.response.data.msg);
        } else if (err.response && err.response.status === 204) {
          console.log("204")
        } else if (err.response && err.response.status === 401) {
          console.log("401")
        } else if (err.response && err.response.status === 403) {
          console.log("403")
        } else {
          setFieldError('password', 'Das Semester konnte nicht gelöscht werden. Bitte beachte, dass alle Module und Kurse dieses Semesters vorher gelöscht werden sollten.');
        } setSubmitting(false);
      });
  }

  return (
    <PasswordConfirmationModal id={semesterId} open={isOpen} onClose={onClose} onDelete={onDelete} />
  );
}
