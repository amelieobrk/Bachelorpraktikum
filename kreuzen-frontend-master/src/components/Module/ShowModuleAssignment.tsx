import React from 'react';
import {Major} from "../../api/university";
import {MajorSection} from "../../api/major";

interface ModuleHasMajor {
  majorId: number
  majorName: string
  majorWide: boolean
  enabledSections: MajorSection[]
}

/**
 * Lists all majors and sections to which the module is assigned.
 * If it is assigned to the whole major, no sections are displayed for the major.
 */
export default function ShowModuleAssignment(props : {
  selectedMajors: Major[]
  selectedMajorSections: MajorSection[]
  availableMajors: Major[]
}) {

  const {selectedMajors, selectedMajorSections, availableMajors} = props;

  const renderedMajorList : Map<number, ModuleHasMajor> = new Map(
    selectedMajors.map(m => [m.id, {
      majorId: m.id,
      majorName: m.name,
      majorWide: true,
      enabledSections: []
    }])
  );
  selectedMajorSections.forEach(section => {
    if (renderedMajorList.has(section.majorId)) {

      const major : ModuleHasMajor | undefined = renderedMajorList.get(section.majorId);
      if (!major) return; // Something went wrong...

      const sections : MajorSection[] = major.enabledSections || [];
      sections.push({...section});
      major.enabledSections = sections;
    } else {
      const major : Major | undefined = availableMajors.find(m => m.id === section.majorId);
      if (major) {
        renderedMajorList.set(major.id, {
          majorId: major.id,
          majorName: major.name,
          majorWide: false,
          enabledSections: [section]
        })
      }
    }
  })

  return (
    <div>
      {
        renderedMajorList.size === 0 && (
          <p>
            Dieses Modul wurde zu keinen Studiengängen oder Studienabschnitten hinzugefügt.
          </p>
        )
      }
      {
        Array.from(renderedMajorList).map(([id, major]) => (
          <div key={id} style={{marginTop: 8}}>
            {major.majorName}
            {
              !major.majorWide && major.enabledSections && (
                <ul>
                  {major.enabledSections.map(section => (
                    <li key={section.id}>{section.name}</li>
                  ))}
                </ul>
              )
            }
          </div>
        ))
      }
    </div>
  )
}

