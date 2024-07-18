import React, {useEffect, useState} from 'react';
import { Button, Modal } from 'react-bootstrap';
import {Major} from "../../api/university";
import {MajorSection} from "../../api/major";
import EditModuleAssignment from "./EditModuleAssignment";
import ShowModuleAssignment from "./ShowModuleAssignment";
import api from "../../api";

/**
 * Modal for assignment of a module to a major or major section.
 */
export default function ModuleAssignmentModal(props : { moduleId: number, universityId: number, isOpen: boolean, onClose: () => void}) {

  const {isOpen, onClose, moduleId, universityId} = props;

  const [moduleHasMajors, setModuleHasMajors] = useState<Major[]>([]);
  const [moduleHasSections, setModuleHasSections] = useState<MajorSection[]>([]);
  const [availableMajors, setAvailableMajors] = useState<Major[]>([]);
  const [editOpen, setEditOpen] = useState(false);

  useEffect(() => {
    if (universityId !== 0) {
      api.university.getMajorsByUniversityId(universityId).then(setAvailableMajors)
    } else {
      setAvailableMajors([])
    }
  }, [universityId])
  useEffect(() => {
    if (moduleId !== 0) {
      api.major.getMajorsByModule(moduleId).then(setModuleHasMajors)
      api.section.getSectionsByModule(moduleId).then(setModuleHasSections)
    } else {
      setModuleHasMajors([]);
      setModuleHasSections([]);
    }
  }, [moduleId])

  const handleToggleMajor = (major: Major) => {
    if (moduleHasMajors.findIndex(m => m.id === major.id) !== -1) {
      // Already selected => Remove
      api.module.removeModuleFromMajor(major.id, moduleId).then(() => {
        setModuleHasMajors(m => m.filter(n => n.id !== major.id));
      })
    } else {
      // Not selected => Add
      api.module.addModuleToMajor(major.id, moduleId).then(() => {
        setModuleHasMajors(m => [...m, major]);
      })
    }
  }
  const handleToggleSection = (section: MajorSection) => {
    if (moduleHasSections.findIndex(s => s.id === section.id) !== -1) {
      // Already selected => Remove
      api.module.removeModuleFromSection(section.id, moduleId).then(() => {
        setModuleHasSections(s => s.filter(n => n.id !== section.id));
      })
    } else {
      // Not selected => Add
      api.module.addModuleToSection(section.id, moduleId).then(() => {
        setModuleHasSections(s => [...s, section]);
      })
    }
  }
  const handleClose = () => {
    setEditOpen(false);
    onClose();
  }

  return (
    <Modal show={isOpen} onHide={handleClose}>
      <Modal.Header>
        <Modal.Title>Studiengänge und Studienabschnitte</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {
          editOpen ? (
            <EditModuleAssignment
              moduleId={moduleId}
              availableMajors={availableMajors}
              onToggleMajor={handleToggleMajor}
              onToggleSection={handleToggleSection}
              selectedMajors={moduleHasMajors}
              selectedMajorSections={moduleHasSections}
            />
          ) : (
            <ShowModuleAssignment
              availableMajors={availableMajors}
              selectedMajors={moduleHasMajors}
              selectedMajorSections={moduleHasSections}
            />
          )
        }
      </Modal.Body>
      <Modal.Footer>
        <Button onClick={() => setEditOpen(x => !x)} className="mr-auto">{editOpen ? 'Zurück' : 'Bearbeiten'}</Button>
        <Button onClick={handleClose}>Schließen</Button>
      </Modal.Footer>
    </Modal>
  );
}
