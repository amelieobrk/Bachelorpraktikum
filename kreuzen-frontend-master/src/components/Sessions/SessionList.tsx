import React from 'react';
import { Table } from 'react-bootstrap';
import {Session} from "../../api/session";
import SessionActions from "./SessionActions";

/**
 * Lists sessions
 *
 * @param props
 */
export default function SessionList(props: {sessions: Session[], onChanged?: () => void}){

  //List all sessions including name, information, status and their options

  const {sessions, onChanged} = props;

  const sessionInfo = ( type: String) : String =>{
    if ( type === "exam"){
    return ("Klausur")}
    if ( type === "practice"){
    return ( "Übung") 
    }
    return (type)
  }
  

    return(
        <Table bordered hover className="table-acitve">
        <thead>
          <tr>
            <th>#</th>
            <th>Name der Session</th>
            <th>Info</th>
            <th>Status</th>
            <th>Optionen</th>
          </tr>
        </thead>
        <tbody>
        {
          sessions.map(session => (
            <tr key={session.id}>
              <td>{session.id}</td>
              <td>{session.name}</td>
              <td>{sessionInfo(session.type)}</td>
              <td>{session.isFinished ? 'abgeschlossen' : 'offen'}</td>
              <td>
                <SessionActions session={session} onChanged={onChanged} />
              </td>
            </tr>
          ))
        }
        </tbody>
      </Table>
    )
}
