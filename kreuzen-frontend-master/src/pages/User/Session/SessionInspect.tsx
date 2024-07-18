import React, { useCallback, useEffect, useState } from 'react';
import { Button, Card } from 'react-bootstrap';
import { Session } from "../../../api/session";
import api from "../../../api";
import SessionActions from "../../../components/Sessions/SessionActions";
import { Link, useHistory } from "react-router-dom";
import { prettyPrintDateTime } from "../../../utils";
import SessionResultsCard from "../../../components/Sessions/SessionResultsCard";
import CardHeader from "../../../components/General/CardHeader";

/**
 * Page to view details about a session like selected courses, isComplete, questions, etc.
 */
export default function SessionInspect(props: { sessionId: number }) {

  const { sessionId } = props;

  const history = useHistory();

  const [session, setSession] = useState<Session | null>(null)
  const [questionCount, setQuestionCount] = useState<number>(0)

  const loadData = useCallback(() => {
    api.session.getSession(sessionId).then(setSession);
    api.session.getQuestionCountOfSession(sessionId).then(setQuestionCount);
  }, [sessionId])

  useEffect(() => {
    loadData();
  }, [loadData])

  const printSessionType = (type: string): string => {
    switch (type.toLowerCase()) {
      case 'exam':
        return 'Klausur';
      case 'practice':
        return 'Übung';
      default:
        return 'Übung';
    }
  }

  return (
    <>
      <Card style={{ marginBottom: 48 }}>
        <Card.Body>

          <CardHeader text={`Session${session ? `: ${session.name} (${session.id})` : ''}`} />

          {
            session ? (
              <>

                <div>
                  <b>Name:</b> {session.name}<br />
                  <b>Anzahl an Fragen:</b> {questionCount}<br />
                  <b>Notizen:</b> {session.notes}<br />
                  <b>Sessiontyp:</b> {printSessionType(session.type)}<br />
                  <b>Zufällige Fragenanordnung:</b> {session.isRandom ? 'ja' : 'nein'}<br />
                  <b>Abgeschlossen:</b> {session.isFinished ? 'ja' : 'nein'}<br />
                  <b>Erstellt:</b> {prettyPrintDateTime(session.createdAt)}<br />
                  <b>Zuletzt bearbeitet:</b> {prettyPrintDateTime(session.updatedAt)}
                </div>

                <div style={{ marginTop: 32 }}>
                  <SessionActions session={session} onDeleted={() => history.push('/user/sessions')} onRestarted={loadData} showInspect={false} />
                  <Button variant="secondary" className="float-right" as={Link} to="/user/sessions">
                    Zurück
                  </Button>
                </div>
              </>
            ) : (
              <>
                Loading...
              </>
            )
          }

        </Card.Body>
      </Card>
      {
        session?.isFinished && <SessionResultsCard sessionId={sessionId} />
      }
    </>
  );
}
