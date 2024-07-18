import React from 'react';
import { Button, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';

/*
* Page: Page not found
*
*/
export default function NotFound() {
  return (
    <Card>
      <Card.Body>
        <Card.Title>
          Page not Found
          </Card.Title>
          Die Seite existiert scheinbar nicht.
          <Button variant="link" as={Link} to="/">
          &#10132; Zurück zur Startseite
          </Button>
      </Card.Body>
    </Card>
  );
}