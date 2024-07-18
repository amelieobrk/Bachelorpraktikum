import React from 'react';
import { Accordion, Button, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import AuthContext from '../../contexts/AuthContext';

/*
* Displays a Guide for Kreuzen (Texts missing)
*
*/
export default function Guide() {
  return (
    <>
      <Card>
        <Card.Body>
          <Card.Title className="text-primary" style={{ marginBottom: 32 }}>
            <AuthContext.Consumer>
              {
                (auth) => `Wobei brauchst du Hilfe, ${auth.user?.username}?`
              }
            </AuthContext.Consumer>
            <Button variant="secondary" as={Link} to="/" className="float-right">Zurück</Button>
          </Card.Title>

          <Accordion>
            <Card>
              <Accordion.Toggle className="bg-primary text-white" as={Card.Header} eventKey="0">
                1. Fragen 'kreuzen'
                </Accordion.Toggle>
              <Accordion.Collapse eventKey="0">
                <Card.Header>Folgt</Card.Header>
              </Accordion.Collapse>
            </Card>
          </Accordion>

          <Accordion>
            <Card>
              <Accordion.Toggle className="text-primary" as={Card.Header} eventKey="1">
                2. Sessions erstellen und Verwalten
                </Accordion.Toggle>
              <Accordion.Collapse eventKey="1">
                <Card.Header>Folgt</Card.Header>
              </Accordion.Collapse>
            </Card>
          </Accordion>

          <Accordion>
            <Card>
              <Accordion.Toggle className="bg-primary text-white" as={Card.Header} eventKey="2">
                3. Fragen hinzufügen
                </Accordion.Toggle>
              <Accordion.Collapse eventKey="2">
                <Card.Header>Folgt</Card.Header>
              </Accordion.Collapse>
            </Card>
          </Accordion>

          <Accordion>
            <Card>
              <Accordion.Toggle className="text-primary" as={Card.Header} eventKey="3">
                4. Nutzung von Tags
                </Accordion.Toggle>
              <Accordion.Collapse eventKey="3">
                <Card.Header>Folgt</Card.Header>
              </Accordion.Collapse>
            </Card>
          </Accordion>
        </Card.Body>
      </Card>
    </>
  );
}