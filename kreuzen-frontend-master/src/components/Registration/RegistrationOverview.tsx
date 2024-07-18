import React, {useState} from "react";
import {University} from "../../api/auth";
import {Major} from "../../api/university";
import {Button, Card, Col, Form, Row} from "react-bootstrap";
import {sortByName} from "../../utils";
import CardHeader from "../General/CardHeader";

interface RegistrationOverviewProps {
  firstName: string
  lastName: string
  username: string
  email: string
  university: University | null
  major: Major[]
  error: string | null
  register: () => void
  onBack: () => void
  submitting: boolean
}

/**
 * This component gives the user an overview of all selected options during registration.
 *
 * @param props
 */
const RegistrationOverview = (props : RegistrationOverviewProps) => {

  const [error, setError] = useState<string | null>(null);
  const [acceptPP, setAcceptPP] = useState(false);
  const [acceptTaC, setAcceptTaC] = useState(false);

  const onRegister = () => {
    setError(null);
    if (acceptPP) {
      if (acceptTaC) {
        props.register();
      } else {
        setError("Um Dich zu registrieren, musst Du die AGB akzeptieren.")
      }
    } else {
      setError("Um Dich zu registrieren, musst Du die Datenschutzerklärung akzeptieren.")
    }
  }

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Zusammenfassung" secondary />

        <p>
          <b>Vorname:</b> {props.firstName}
        </p>
        <p>
          <b>Nachname:</b> {props.lastName}
        </p>
        <p>
          <b>Username:</b> {props.username}
        </p>
        <p>
          <b>Email:</b> {props.email}
        </p>
        <p>
          <b>Universität:</b> {props.university?.name}
        </p>
        <span><b>{props.major.length > 1 ? 'Studiengänge' : 'Studiengang'}:</b></span>
        <ul>
          {props.major.sort(sortByName).map(major => <li key={major.id}>
            {major.name}
            <ul>
              {(major.sections || []).sort(sortByName).map(section => <li key={section.id}>{section.name}</li>)}
            </ul>
          </li>)}
        </ul>

        <Form.Check
          type="switch"
          id="accept-pp"
          label="Ich akzeptiere die Datenschutzerklärung"
          checked={acceptPP}
          onChange={() => setAcceptPP(x => !x)}
          isInvalid={(error != null && !acceptPP) || props.error != null}
          data-testid="pp-slider"
        />
        <Form.Check
          type="switch"
          id="accept-toc"
          label="Ich akzeptiere die AGB"
          checked={acceptTaC}
          onChange={() => setAcceptTaC(x => !x)}
          isInvalid={(error != null && !acceptTaC) || props.error != null}
          data-testid="tac-slider"
        />

        <Form.Text style={{ marginTop: 16 }}>
          {error}
          {props.error}
        </Form.Text>

        <div style={{ marginTop: 16 }}>
          <Row>
            <Col>
              <Button size="sm" variant="outline-primary" block onClick={props.onBack} disabled={props.submitting}>
                Zurück
              </Button>
            </Col>
            <Col>
              <Button size="sm" variant="primary" block onClick={onRegister} disabled={props.submitting} data-testid="register-button">
                Registrieren
              </Button>
            </Col>
          </Row>
        </div>

      </Card.Body>
    </Card>
  )
}

export default RegistrationOverview;
