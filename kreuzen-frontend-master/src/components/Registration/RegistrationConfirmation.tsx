import React from "react";
import {Button, Card} from "react-bootstrap";
import {Link} from "react-router-dom";
import CardHeader from "../General/CardHeader";

interface ConfirmRegistrationProps {
  firstName: string
  email: string
}

/**
 * Confirmation that the registration was successful.
 *
 * @param props
 */
const RegistrationConfirmation = (props : ConfirmRegistrationProps) => {
  return (
    <Card>
      <Card.Body>
        <CardHeader text="Registrierung abgeschlossen" secondary />

        <p>
          Willkommen {props.firstName}!
        </p>
        <p>
          Wir haben Dir eine E-Mail an &lt;{props.email}&gt; geschickt, damit Du Deine Email-Adresse bestätigen kannst. <br/>
          Sobald Deine Email-Adresse bestätigt ist, kannst du anfangen zu kreuzen.
        </p>

        <div style={{ marginTop: 16 }}>
          <Button size="sm" variant="outline-secondary" block as={Link} to="/confirm-email">
            Account freischalten
          </Button>
        </div>

      </Card.Body>
    </Card>
  )
}

export default RegistrationConfirmation;