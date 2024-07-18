import React, {useEffect, useState} from 'react';
import { Form } from 'react-bootstrap';
import api from '../../api';
import {Major} from "../../api/university";
import {MajorSection} from "../../api/major";

/**
 * Element to assign a module to one major or the sections of the major.
 */
export default function ModuleAssignMajor(props : {
  major: Major
  majorSelected: boolean
  selectedSections: MajorSection[]
  onToggleMajor: (major: Major) => void
  onToggleSection: (section: MajorSection) => void
}) {

  const {major, majorSelected, selectedSections, onToggleMajor, onToggleSection} = props;
  const majorId = major.id;

  const selectedSectionIds = selectedSections.map(s => s.id);

  const [sections, setSections] = useState<MajorSection[]>([]);

  useEffect(() => {
    api.major.getSectionsByMajor(majorId).then(setSections);
  }, [majorId])

  return (
    <div>
      <div>
        <Form.Check
          type="checkbox"
          label={major.name}
          checked={majorSelected}
          onChange={() => onToggleMajor(major)}
        />
      </div>
      <div style={{paddingLeft: 24}}>
        {
          sections.map(section => (
            <Form.Check
              key={section.id}
              type="checkbox"
              label={section.name}
              checked={selectedSectionIds.includes(section.id) || majorSelected}
              onChange={() => onToggleSection(section)}
              disabled={majorSelected}
            />
          ))
        }
      </div>
    </div>
  )
}

