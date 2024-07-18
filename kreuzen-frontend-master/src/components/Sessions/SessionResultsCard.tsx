import React, { useEffect, useState } from 'react';
import {Card, ListGroup, ListGroupItem} from 'react-bootstrap';
import api from '../../api';
import { QuestionResult } from "../../api/session";
import { shortenText } from "../../utils";
import { Link } from "react-router-dom";
import CardHeader from "../General/CardHeader";

/**
 * Card displaying the results of a session.
 */
export default function SessionResultsCard(props: {
  sessionId: number
}) {

  const { sessionId } = props;

  const [results, setResults] = useState<QuestionResult[]>([])

  useEffect(() => {
    api.session.getSessionResults(sessionId).then(setResults);
  }, [sessionId])

  const getTextColor = (achievedPoints: number, availablePoints: number): string => {
    if (achievedPoints === availablePoints) {
      return 'text-success';
    } else if (achievedPoints > 0) {
      return 'text-warning';
    } else {
      return 'text-danger';
    }
  }

  const achievedPoints: number = results.reduce((v, r) => v + r.points, 0);
  const availablePoints: number = results.reduce((v, r) => v + r.question.points, 0);

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Auswertung" secondary />
        <ListGroup>
          {
            results.map(result => (
              <ListGroupItem style={{ cursor: 'pointer' }} key={result.localId}>
                <Link to={`/user/sessions/${sessionId}/review/${result.localId}`}>
                  <span className={getTextColor(result.points, result.question.points)}>
                    (#{result.localId}) {shortenText(result.question.text)}
                  </span>
                </Link>
                <span style={{ marginLeft: 16 }} className="float-right text-secondary">
                  ({result.points} / {result.question.points})
                </span>
              </ListGroupItem>
            ))
          }
        </ListGroup>
        <p style={{ marginTop: 16, marginBottom: 0 }}>
          {achievedPoints} von {availablePoints} Punkten erreicht.
        </p>
      </Card.Body>
    </Card>
  );
}
