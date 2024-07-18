import React from 'react';
import { Button, Card, Col, Form, ListGroup, Row } from 'react-bootstrap';
import CardHeader from "../../components/General/CardHeader";

/**
 * Page listing all questions
 */
export default function Fragen() {

  return (
    <>
      <Card>
        <Card.Body>
          <CardHeader text="Verwaltung aller Klausurfragen" />
          <Row>
            <Col>
              <Form.Group>
                <Form.Control as="select">
                  <option>Unbestätigte Fragen anzeigen</option>
                  <option>Nach Modul sortieren</option>
                  <option>Nach Semester sortieren</option>
                </Form.Control>
              </Form.Group>
            </Col>
            <Col>
              <Button>Sortieren</Button>
            </Col>
          </Row>
          <Row>
            <ListGroup variant="flush">

            </ListGroup>
          </Row>
        </Card.Body>
      </Card>
    </>
  );
}
