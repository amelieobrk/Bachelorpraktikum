import React, { useEffect, useState } from 'react';
import { Jumbotron } from 'react-bootstrap';
import { Button, Card, Col, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import api from '../../api';
import { Hint } from '../../api/hint';
import QuickSelection from '../../components/Home/QuickSelection';
import AuthContext from '../../contexts/AuthContext';

export default function HomeLogin(props: { userId: number }) {

  const { userId } = props;
  const [hint, setHint] = useState<Hint>();

  // Gets a Hint when loading the page for the first time
  useEffect(() => {
    api.hint.getRandomHint()
      .then(
        (res: any) => {
          setHint(res);
        })
      .catch((err) => {
        if (err.response && err.response.data && err.response.data.msg) {
          console.error("Could not load hint!")
        }
      })
  }, [])

  /*
  * Requests a new Hint
  */
  function nextHint() {
    api.hint.getRandomHint()
      .then(
        (res: any) => {
          setHint(res);
        })
  }

  return (
    <>
      <Card>
        <Card.Body>
          <Card.Title>
            <h1 className="text-center">HOME</h1>
          </Card.Title>
          <Row>
            <Col xs={12} md={8} style={{ marginRight: 0 }}>
              <Card>
                <Card.Body>
                  <Jumbotron style={{ marginBottom: 0 }}>
                    <AuthContext.Consumer>
                      {
                        (auth) => <h1 className="text-center">Willkommen, {auth?.user?.username}!</h1>
                      }
                    </AuthContext.Consumer>
                    <div className="text-center">
                      Was möchtest du als nächstes tun?
                      </div>
                  </Jumbotron>
                </Card.Body>
              </Card>
            </Col>
            <Col xs={6} md={4} style={{ marginLeft: 0, marginBottom: 0 }}>
              <Card style={{ padding: '3%' }}>
                <Card>
                  <Card.Body>
                    <Button block variant="primary" as={Link} to="/help/guide">&#10143; Hier gehts zum KREUZEN-Leitfaden</Button>
                    <Button block variant="primary" as={Link} to="/help/faq">&#10143; Hier gehts zu den FAQ</Button>
                  </Card.Body>
                </Card>
                <Card>
                  <Card.Body>
                    <Card.Title>
                      <div className="text-primary">
                        Tipp des Tages:
                      </div>
                      <Button variant="primary" onClick={nextHint} style={{ float: 'right' }}><i className="fas fa-redo" /></Button>
                    </Card.Title>
                    {hint?.text}
                  </Card.Body>
                </Card>
              </Card>
            </Col>
          </Row>
          <Row>
            <Col>
              <Card>
                <Card.Body>
                  <Card>
                    <QuickSelection userId={userId} />
                  </Card>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </>
  );
}