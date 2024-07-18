import React from 'react';
import { Button, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';


/*
* Page for denied Accesses
*
*/
export default function ZugriffAbgelehnt() {
  return (
    <>
      <Card>
        <Card.Body>
          <Card.Title>
            No Permission
          </Card.Title>
          Diese Seite ist nicht für alle Rollen verfügbar. Bitte wende dich an einen Admin, falls du Zugriff zu dieser Seite benötigst.
          <Button variant="link" as={Link} to="/">
          &#10132; Zurück zur Startseite
          </Button>
        </Card.Body>
      </Card>
    </>
  );
}