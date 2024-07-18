import React from 'react';
import { Button, Modal } from 'react-bootstrap';
import api from '../../api';
import {Session} from "../../api/session";

/**
 * Modal to delete a session.
 */
export default function DeleteSessionModal(props : {session: Session, isOpen: boolean, onClose: () => void, onDeleted?: () => void}) {

  const {isOpen, onClose, session, onDeleted} = props;

  const handleDelete = () => {
    api.session.deleteSession(session.id).then(() => {
      onClose();
      if (onDeleted) onDeleted();
    })
  }

  return (
    <Modal show={isOpen} onHide={onClose}>
      <Modal.Header>
        <Modal.Title>Session löschen</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        Bist Du sicher, dass Du die Session "{session.name}" löschen willst?<br/>
        Deine Antworten in der Session können nicht wieder hergestellt werden.
      </Modal.Body>
      <Modal.Footer>
        <Button onClick={handleDelete} variant="danger">Löschen</Button>
        <Button onClick={onClose} variant="secondary">Schließen</Button>
      </Modal.Footer>
    </Modal>
  );
}
