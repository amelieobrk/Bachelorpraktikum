import React, { useEffect, useState } from "react";
import { Button, Carousel, Form } from "react-bootstrap";
import { Link, useHistory } from "react-router-dom";
import api from "../../api";
import { Session } from "../../api/session";
import banner from "../../Grafiken/Carousel.png";
import { sortByName } from "../../utils";

export default function QuickSelection(this: any, props: { userId: number }) {

  const { userId } = props;
  const history = useHistory();
  const [ses, setSes] = useState<Session[]>([]);
  const [selectedSession, setSelectedSession] = useState<string>('-1')

  // Gets all Sessions of the current user
  useEffect(() => {

    api.user.getSessionsByUser(userId)
      .then(
        (res: Session[]) => {
          setSes(res);
          setSelectedSession(String(res[0].id))
        })
  }, [userId])

  /*
  *
  * Carousel for HOME
  * 
  */
  const handleLoadSession = () => {
    if (selectedSession !== '-1') {
      history.push(`/user/sessions/${selectedSession}/learn`)
    }
  }

  return (
    <>
      <Carousel>
        <Carousel.Item>
          <img
            className="d-block w-100"
            src={banner}
            alt="First slide"
          />
          <Carousel.Caption>
            <h4>Sessions lernen</h4>
            <br />
            <p>
              <Form className="align-items-center">
                <Form.Label htmlFor="inlineFormCustomSelect" srOnly>
                  Session auswählen
                </Form.Label>
                <Form.Control
                  as="select"
                  className="mr-sm-2"
                  id="select"
                  custom
                  onChange={e => setSelectedSession(e.target.value)}
                  value={selectedSession}
                >
                  {
                    ses.length === 0 && (
                      <option value="-1">
                        Keine Sessions verfügbar
                      </option>
                    )
                  }
                  {ses.sort(sortByName).map((element: Session) =>
                    <option key={element.id} value={element.id}>
                      {element.name}
                    </option>
                  )}
                </Form.Control>
                <br />
                <br />
                <Button variant="outline-light" type="submit" onClick={handleLoadSession}>Diese Session lernen</Button>
              </Form>
            </p>
          </Carousel.Caption>
        </Carousel.Item>
        <Carousel.Item>
          <img
            className="d-block w-100"
            src={banner}
            alt="Second slide"
          />
          <Carousel.Caption>
            <h4>Fragen hinzufügen</h4>
            <p>
              Diese Plattform lebt davon, dass Studierende ihre Fragen eingeben.
              <br />
              <br />
              <Button variant="outline-light" as={Link} to="/user/questions/add">
                Fragen eingeben
              </Button>
            </p>
          </Carousel.Caption>
        </Carousel.Item>
        <Carousel.Item>
          <img
            className="d-block w-100"
            src={banner}
            alt="Third slide"
          />
          <Carousel.Caption>
            <h4>Meine Einstellungen</h4>
            <br />
            <p>
              <Button variant="outline-light" as={Link} to="/me/settings">
                Meine Einstellungen und Benutzerdaten verwalten.
              </Button>
              <br />
              <br />
            </p>
          </Carousel.Caption>
        </Carousel.Item>
      </Carousel>
    </>
  )
}
