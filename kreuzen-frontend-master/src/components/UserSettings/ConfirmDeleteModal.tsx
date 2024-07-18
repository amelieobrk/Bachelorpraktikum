import React, {useState} from 'react';
import { Form } from 'react-bootstrap';
import {Button, Modal} from "react-bootstrap";

interface ConfirmDeleteModalParams {
  open: boolean
  error: string | null
  onClose: () => void
  onConfirm: (password: string) => void
}

/**
 * Modal to confirm the deletion of an account
 *
 * @param params
 */
export default function ConfirmDeleteModal(params : ConfirmDeleteModalParams) {

  const {open, error, onClose, onConfirm} = params;

  const [username, setUsername] = useState('');

  return (
    <Modal show={open} onHide={onClose}>
      <Modal.Header closeButton>
        <Modal.Title>Account löschen</Modal.Title>
      </Modal.Header>

      <Modal.Body>
        <p>Bitte bestätige die Löschung des Accounts, indem Du den Username eingibst.</p>
        <p>Ein gelöschter Account kann nicht wieder hergestellt werden.</p>
        {
          error && (
            <p>{error}</p>
          )
        }

        <Form.Group>
          <Form.Control placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
        </Form.Group>

      </Modal.Body>

      <Modal.Footer>
        <Button variant="secondary" onClick={onClose}>Close</Button>
        <Button variant="danger" onClick={() => onConfirm(username)}>Confirm Delete</Button>
      </Modal.Footer>
    </Modal>
  )
}
