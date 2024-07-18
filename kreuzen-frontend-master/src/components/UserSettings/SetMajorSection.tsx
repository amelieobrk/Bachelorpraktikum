import React, {ChangeEvent} from 'react';
import {Form} from 'react-bootstrap';
import {Major} from "../../api/university";
import {MajorSection} from "../../api/major";

interface SetMajorSectionProps {
  availableMajors: Major[]
  availableSections: MajorSection[]
  sectionIds: number[]
  majorIds: number[]
  onRemoveSection: (section: MajorSection) => void
  onAddSection: (section: MajorSection) => void
  loaded: boolean
}

/**
 * Form to select major sections
 */
export default function SetMajorSection(props : SetMajorSectionProps) {

  const {availableMajors, availableSections, sectionIds, majorIds, onRemoveSection, onAddSection, loaded} = props;

  const toggleMajorSection = (e : ChangeEvent<HTMLInputElement>) => {

    const id : number | null = Number.parseInt(e.target.id);

    if (id != null) {
      const section : MajorSection | undefined = availableSections.find(s => s.id === id);
      if (section) {
        if (sectionIds.includes(id)) {
          onRemoveSection(section)
        } else {
          onAddSection(section)
        }
      }
    }
  }

  const selectedMajors = availableMajors.filter(m => majorIds.includes(m.id));

  return (
    <>
      {
        loaded && selectedMajors.map(major => (
          <div key={major.id}>
            <b>{major.name}</b>
            {
              availableSections
                .filter(section => section.majorId === major.id)
                .map(section => <Form.Check
                  type="checkbox"
                  key={section.id}
                  id={String(section.id)}
                  data-testid={String(section.id)}
                  label={section.name}
                  checked={sectionIds.includes(section.id)}
                  onChange={toggleMajorSection}
                />)
            }
          </div>
        ))
      }
    </>
  );
}
