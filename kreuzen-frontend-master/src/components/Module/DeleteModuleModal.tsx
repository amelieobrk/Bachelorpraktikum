import React from 'react';
import api from '../../api';
import PasswordConfirmationModal from '../General/PasswordConfirmationModal';

/**
 * Modal to delete a module.
 */
export default function DeleteModuleModal(props : {moduleId: number, isOpen: boolean, onClose: () => void, onDeleted: () => void}) {

  const {isOpen, onClose, onDeleted, moduleId} = props;

  // Deletion process (Correct Password has been provided)
  function onDelete(id: number, password: string, setSubmitting: (isSubmitting: boolean) => void, setFieldError: (field: string, message: string | undefined) => void): void {
    api.module.deleteModule(id, password)
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
          setFieldError('password', 'Das Modul konnte nicht gelöscht werden. Bitte beachte, dass alle Kurse und Tags dieses Moduls vorher entfernt werden sollten.');
        } setSubmitting(false);
      });
  }

  return (
    <PasswordConfirmationModal id={moduleId} open={isOpen} onClose={onClose} onDelete={onDelete} />
  );
}
