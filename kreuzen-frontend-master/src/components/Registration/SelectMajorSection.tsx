import React, {ChangeEvent, useEffect, useState} from "react";
import {Major} from "../../api/university";
import {MajorSection} from "../../api/major";
import api from "../../api";
import {Button, Card, Col, Form, Row} from "react-bootstrap";
import {sortByName} from "../../utils";
import CardHeader from "../General/CardHeader";

interface SelectMajorSectionProps {
  majors: Major[]
  setMajors: React.Dispatch<React.SetStateAction<Major[]>>
  onNext: () => void
  onBack: () => void
}

/**
 * Component letting the user select their current section in their major. Only available sections are displayed.
 *
 * @param props
 */
const SelectMajorSection = (props: SelectMajorSectionProps) => {

  const [availableMajorSections, setAvailableMajorSections] = useState<Major[]>([])

  const selectedMajorSectionIds : number[] = props.majors
    .map(m => m.sections || [])
    .flat(1)
    .map(s => s.id);

  const handleChange = (e : ChangeEvent<HTMLInputElement>) => {

    const id : number = parseInt(e.target.id);

    if (selectedMajorSectionIds.includes(id)) {
      props.setMajors((majors) => {
        return majors.map(m => {
          return {
            ...m,
            sections: (m.sections || []).filter(s => s.id !== id)
          }
        });
      })
    } else {

      const major : Major | undefined = availableMajorSections.find(m => (m.sections || []).find(x => x.id === id));
      if (major) {
        const currentMajor : Major | undefined = props.majors.find(m => m.id === major.id);
        const section : MajorSection | undefined = (major.sections || []).find(x => x.id === id);
        if (section) {
          props.setMajors((majors) => {
            return [
              ...majors.filter(m => m.id !== major.id),
              {
                ...major,
                sections: [
                  ...(currentMajor?.sections || []),
                  section
                ]
              }
            ];
          })
        }
      }
    }
  }

  useEffect(() => {
    // Load available majors when university id changes
    if (props.majors && props.majors.length > 0) {

      Promise.all(props.majors.map(major => api.major.getSectionsByMajor(major.id))).then((res) => {

        const sections : Major[] = res.map((sec, i) => {
          const mws : Major = {
            id: props.majors[i].id,
            name: props.majors[i].name,
            sections: sec.sort(sortByName)
          };
          return mws;
        });

        setAvailableMajorSections(sections);
      })
    }
  }, [props.majors])

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Wähle Deinen Studienabschnitt" secondary />

        {
          availableMajorSections.length === 0 && (
            "Keine Studienabschnitte verfügbar..."
          )
        }

        {
          availableMajorSections
            .sort(sortByName)
            .map(major => (
              <div key={major.id}>
                <span>{major.name}</span>
                {
                  (major.sections || [])
                    .sort(sortByName)
                    .map(section => <Form.Check
                      type="checkbox"
                      key={section.id}
                      id={String(section.id)}
                      label={section.name}
                      checked={selectedMajorSectionIds.includes(section.id)}
                      onChange={handleChange}
                      data-testid={section.id}
                    />)
                }
              </div>
            ))
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

export default SelectMajorSection;
