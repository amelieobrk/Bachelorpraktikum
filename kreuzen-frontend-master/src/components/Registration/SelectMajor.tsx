import React, {ChangeEvent, useEffect, useState} from "react";
import {Major} from "../../api/university";
import {University} from "../../api/auth";
import api from "../../api";
import {Button, Card, Col, Form, Row} from "react-bootstrap";
import {sortByName} from "../../utils";
import CardHeader from "../General/CardHeader";

interface SelectMajorProps {
  majors: Major[]
  university: University | null
  setMajors: React.Dispatch<React.SetStateAction<Major[]>>
  error: string | null
  onNext: () => void
  onBack: () => void
}

/**
 * Component letting the user select their majors.
 *
 * @param props
 */
const SelectMajor = (props : SelectMajorProps) => {

  const [availableMajors, setAvailableMajors] = useState<Major[]>([]);

  const selectedMajorsIds : number[]= props.majors.map(m => m.id);

  const handleChange = (e : ChangeEvent<HTMLInputElement>) => {

    const id : number = parseInt(e.target.id);

    if (selectedMajorsIds.includes(id)) {
      props.setMajors((majors) => {
        return majors.filter(m => m.id !== id);
      })
    } else {

      const major : Major | undefined = availableMajors.find(m => m.id === id);

      if (major) {
        props.setMajors((majors) => {
          return [
            ...majors,
            major
          ];
        })
      }
    }
  }

  const {university, setMajors} = props;

  useEffect(() => {
    // Load available majors when university id changes
    if (university) {
      api.university.getMajorsByUniversity(university).then((res) => {
        setAvailableMajors(res.sort(sortByName));
        // check whether current majors are in list.
        setMajors(majors => {
          return majors.filter(m => res.findIndex(n => n.id === m.id) !== -1);
        })
      })
    }
  }, [university, setMajors])

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Wähle Deinen Studiengang" secondary />

        {
          availableMajors.length === 0 && (
            "Keine Studiengänge verfügbar..."
          )
        }

        {
          availableMajors.map(major => <Form.Check
            type="checkbox"
            key={major.id}
            id={String(major.id)}
            data-testid={String(major.id)}
            label={major.name}
            checked={selectedMajorsIds.includes(major.id)}
            onChange={handleChange}
          />)
        }

        {
          props.error
        }

        <div style={{ marginTop: 16 }}>
          <Row>
            <Col>
              <Button size="sm" variant="outline-primary" block onClick={props.onBack}>
                Zurück
              </Button>
            </Col>
            <Col>
              <Button size="sm" variant="primary" block onClick={props.onNext} data-testid="next-button">
                Weiter
              </Button>
            </Col>
          </Row>
        </div>

      </Card.Body>
    </Card>
  )
}

export default SelectMajor;
