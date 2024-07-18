import React from 'react';
import {Major} from "../../api/university";
import {MajorSection} from "../../api/major";
import { sortByName } from '../../utils';
import ModuleAssignMajor from './ModuleAssignMajor';

/**
 * View to assign a module to majors and major sections.
 */
export default function EditModuleAssignment(props : {
  moduleId: number,
  selectedMajors: Major[]
  selectedMajorSections: MajorSection[]
  availableMajors: Major[]
  onToggleMajor: (major: Major) => void
  onToggleSection: (section: MajorSection) => void
}) {

  const {selectedMajors, selectedMajorSections, availableMajors, onToggleMajor, onToggleSection} = props;

  const selectedMajorsIds = selectedMajors.map(m => m.id);

  return (
    <div>
      {
        availableMajors.sort(sortByName).map(major => (
          <ModuleAssignMajor
            key={major.id}
            major={major}
            onToggleMajor={onToggleMajor}
            onToggleSection={onToggleSection}
            majorSelected={selectedMajorsIds.includes(major.id)}
            selectedSections={selectedMajorSections.filter(s => s.majorId === major.id)}
          />
        ))
      }
    </div>
  )
}

