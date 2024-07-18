import React from 'react';
import { Button, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';

/*
* Dummy-Page for pages in development
*
*/
export default function InProgress() {
  return (
    <>
      <Card>
        <Card.Body>
          <Card.Title>
            Page in Progress
          </Card.Title>
          Diese Seite wird noch implementiert und wird in nächster Zeit verfügbar sein.
          <Button variant="link" as={Link} to="/">
          &#10132; Zurück zur Startseite
          </Button>
        </Card.Body>
      </Card>
    </>
  );
}