import React, { useEffect, useState } from 'react';
import { Button, Card, ListGroup, ListGroupItem } from 'react-bootstrap';
import { Module } from "../../api/modules";
import api from '../../api';
import DeleteModuleModal from "./DeleteModuleModal";
import EditModuleModal from "./EditModuleModal";
import TagListModal from "../Tags/TagListModal";
import ModuleAssignmentModal from "./ModuleAssignmentModal";
import ModuleCourses from "../General/ModuleCourses";

/**
 * Card displaying a module and options to edit and delete it.
 */
export default function ModuleCard(props : {
  moduleId: number | null | undefined,
  onUpdated: (() => void) | undefined | null,
  onDeleted: (() => void) | undefined | null
}) {

  const {moduleId, onUpdated, onDeleted} = props;

  const [module, setModule] = useState<Module | null>(null)
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [tagsOpen, setTagsOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [assignmentOpen, setAssignmentOpen] = useState(false);
  const [coursesOpen, setCoursesOpen] = useState(false);

  useEffect(() => {
    if (moduleId) {
      api.module
        .getModuleById(moduleId)
        .then(setModule)
    } else {
      setModule(null)
    }
  }, [moduleId])

  const reload = () => {
    if (moduleId) {
      api.module
        .getModuleById(moduleId)
        .then(setModule)
    }
  }
  if (!moduleId) {
    return (
      <Card>
        <Card.Title style={{ padding: '5%' }}><h2>Kein Modul ausgewählt.</h2></Card.Title>
      </Card>
    );
  } else {
    if (!module) {
      return (
        <Card>
          <Card.Title style={{padding: '5%'}}><h3>Modul wird geladen.</h3></Card.Title>
        </Card>
      );
    } else {
      return (
        <>
          <Card>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroupItem>
                  <b>Bezeichnung:</b> {module?.name}
                </ListGroupItem>
                <ListGroupItem>
                  <b>ID:</b> {module?.id}
                </ListGroupItem>
                <ListGroupItem>
                  <b>ID der Universität:</b> {module?.universityId}
                </ListGroupItem>
                <ListGroupItem>
                  <b>Universitätsübergreifend:</b> {module?.universityWide ? "Ja" : "Nein"}
                </ListGroupItem>
                <ListGroupItem>
                  <Button variant="link" onClick={() => setCoursesOpen(true)}>&#10132; Kurse dieses Moduls verwalten</Button>
                </ListGroupItem>
                <ListGroupItem>
                  <Button variant="link" onClick={() => setTagsOpen(true)}>&#10132; Tags dieses Moduls einsehen</Button>
                </ListGroupItem>
                <ListGroupItem>
                  <Button variant ="link" onClick={() => setAssignmentOpen(true)}>&#10132; Studiengänge und Studienabschnitte dieses Moduls verwalten</Button>
                </ListGroupItem>
              </ListGroup>
            </Card.Body>
            <Card.Footer>
              <Button
                onClick={() => setEditOpen(true)}
                size="sm"
                variant="secondary"
                style={{ margin: 3 }}
              >
                Modul bearbeiten
              </Button>
              <Button onClick={() => setDeleteOpen(true)} size="sm" variant="danger" style={{ margin: 3 }}>
                Modul löschen
              </Button>
            </Card.Footer>
          </Card>
          <TagListModal
            moduleId={moduleId}
            isOpen={tagsOpen}
            onClose={() => setTagsOpen(false)}
          />
          <DeleteModuleModal
            moduleId={moduleId}
            isOpen={deleteOpen}
            onClose={() => setDeleteOpen(false)}
            onDeleted={() => {
              setDeleteOpen(false);
              if (onDeleted) onDeleted();
            }}
          />
          <EditModuleModal
            moduleId={moduleId}
            moduleName={module.name}
            universityId={module.universityId}
            universityWide={module.universityWide}
            isOpen={editOpen}
            onClose={() => setEditOpen(false)}
            onEdited={() => {
              reload();
              setEditOpen(false);
              if (onUpdated) onUpdated();
            }}
          />
          <ModuleAssignmentModal
            universityId = {module?.universityId || 0}
            isOpen = {assignmentOpen}
            onClose = {() => setAssignmentOpen(false)}
            moduleId={moduleId}
          />
          <ModuleCourses
            moduleID={moduleId}
            moduleName={module?.name}
            open={coursesOpen}
            onClose={() => setCoursesOpen(false)}
          />
        </>
      );
    }
  }

}
