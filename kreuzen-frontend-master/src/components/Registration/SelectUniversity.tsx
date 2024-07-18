import React, {ChangeEvent} from "react";
import {University} from "../../api/auth";
import {Button, Card, Col, Form, Row} from "react-bootstrap";
import {sortByName} from "../../utils";
import CardHeader from "../General/CardHeader";

interface SelectUniversityProps {
  availableUniversities: University[]
  university: University | null
  setUniversity: (uni : University) => void
  onNext: () => void
  onBack: () => void
}

/**
 * In case multiple universities share a common domain, this component is used to give the user the choice to select
 * their university.
 *
 * @param props
 */
const SelectUniversity = (props: SelectUniversityProps) => {
  const onChange = (e : ChangeEvent<HTMLSelectElement>) => {
    const id : number = parseInt(e.target.value);

    const uni : University | undefined = props.availableUniversities.find(u => u.id === id);
    if (uni) {
      props.setUniversity(uni);
    }
  }

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Wähle Deine Universität" secondary />

        <Form.Group>
          <Form.Label>Universität</Form.Label>
          <Form.Control as="select" defaultValue={undefined} value={props.university?.id} onChange={onChange} data-testid="university-select">
            <option value={undefined}>Wähle Deine Universität...</option>
            {
              props.availableUniversities.sort(sortByName).map(uni => <option key={uni.id} value={uni.id}>{uni.name}</option>)
            }
          </Form.Control>
        </Form.Group>

        <div style={{ marginTop: 16 }}>
          <Row>
            <Col>
              <Button size="sm" variant="outline-primary" block onClick={props.onBack}>
                Zurück
              </Button>
            </Col>
            <Col>
              <Button data-testid="next-button" size="sm" variant="primary" block onClick={props.onNext}>
                Weiter
              </Button>
            </Col>
          </Row>
        </div>

      </Card.Body>
    </Card>
  )
}

export default SelectUniversity;
