import React, { useState } from 'react';
import { Button } from 'react-bootstrap';
import DeleteSessionModal from "./DeleteSessionModal";
import {Session} from "../../api/session";
import RestartSessionModal from "./RestartSessionModal";
import {Link} from "react-router-dom";

/**
 * Actions for a session: learn, delete, redo, inspect
 */
export default function SessionActions(props: {
  session: Session,
  onChanged?: () => void,
  onDeleted?: () => void,
  onRestarted?: () => void,
  showInspect?: boolean
}){

  const {session, onChanged, onDeleted, onRestarted, showInspect} = props;

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [restartOpen, setRestartOpen] = useState(false);

  // Buttons to create, delete, play and show session

  return(
    <>
      {
        !session.isFinished && (
          <Button variant="success" size="sm" style={{marginLeft: 3}} as={Link} to={`/user/sessions/${session.id}/learn`}>
            <i className="fas fa-play" />
          </Button>
        )
      }
      <Button variant="warning" style={{marginLeft: 3}} size="sm" onClick={() => setRestartOpen(true)}>
        <i className="fas fa-redo" />
      </Button>
      <Button variant="danger" style={{marginLeft: 3}} size="sm" onClick={() => setDeleteOpen(true)}>
        <i className="fas fa-trash" color="red" />
      </Button>
      {
        (showInspect == null || showInspect) && (
          <Button variant="primary" style={{marginLeft: 3}} size="sm" as={Link} to={`/user/sessions/${session.id}`}>
            <i className="fas fa-search" />
          </Button>
        )
      }
      <DeleteSessionModal session={session} isOpen={deleteOpen} onClose={() => setDeleteOpen(false)} onDeleted={onDeleted || onChanged}/>
      <RestartSessionModal session={session} isOpen={restartOpen} onClose={() => setRestartOpen(false)} onRestarted={onRestarted || onChanged}/>
    </>
  )
}