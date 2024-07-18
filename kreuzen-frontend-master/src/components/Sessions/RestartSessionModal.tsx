import React from 'react';
import { Button, Modal } from 'react-bootstrap';
import api from '../../api';
import {Session} from "../../api/session";

/**
 * Modal to restart a session.
 */
export default function RestartSessionModal(props : {session: Session, isOpen: boolean, onClose: () => void, onRestarted?: () => void}) {

  const {isOpen, onClose, session, onRestarted} = props;

  const handleRestart = () => {
    api.session.resetSession(session.id).then(() => {
      onClose();
      if (onRestarted) onRestarted();
    })
  }

  return (
    <Modal show={isOpen} onHide={onClose}>
      <Modal.Header>
        <Modal.Title>Session reset</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        Bist Du sicher, dass Du die Session "{session.name}" neu starten willst?<br/>
        Deine Antworten in der Session können nicht wieder hergestellt werden.
      </Modal.Body>
      <Modal.Footer>
        <Button onClick={handleRestart} variant="warning">Reset</Button>
        <Button onClick={onClose} variant="secondary">Schließen</Button>
      </Modal.Footer>
    </Modal>
  );
}
