import React from 'react';
import { Button, Card, ListGroup, ListGroupItem } from 'react-bootstrap';
import { Link } from 'react-router-dom';

/*
* Page that navigates to FAQ/Guide
*
*/
export default function Help() {
  return (
    <>
      <Card className="bg-primary">
        <Card.Body>
          <Card.Title className="text-white">
            <h1 className="text-white">
              Hilfe
            </h1>
          </Card.Title>
          <ListGroup>
            <ListGroupItem>
              <Button variant="primary" as={Link} to="/help/guide">&#10143;</Button>
              &nbsp; Der Kreuzen-Guide erklärt dir die grundlegenden Funktionalitäten.
            </ListGroupItem>
            <ListGroupItem>
              <Button variant="primary" as={Link} to="/help/faq">&#10143;</Button>
              &nbsp; In den FAQ findest du häufig gestellte Fragen zu der Plattform 'Kreuzen'.
            </ListGroupItem>
          </ListGroup>
        </Card.Body>
      </Card>
    </>
  );
}