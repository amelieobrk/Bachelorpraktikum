import React, { useEffect, useState } from 'react';
import { Button, Card, ListGroup, ListGroupItem } from 'react-bootstrap';
import { Semester } from "../../api/semester";
import api from '../../api';
import DeleteSemesterModal from "./DeleteSemesterModal";
import EditSemesterModal from "./EditSemesterModal";

/**
 * Card displaying a semester and options to edit and delete it.
 */
export default function SemesterCard(props : {
  semesterId: number | null | undefined,
  onUpdated: (() => void) | undefined | null,
  onDeleted: (() => void) | undefined | null
}) {

  const [semester, setSemester] = useState<Semester | null>(null)
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);

  const {semesterId, onUpdated, onDeleted} = props;

  useEffect(() => {
    if (semesterId) {
      api.semester
        .getSemesterById(semesterId)
        .then(setSemester)
    } else {
      setSemester(null)
    }
  }, [semesterId])

  const reload = () => {
    if (semesterId) {
      api.semester
        .getSemesterById(semesterId)
        .then(setSemester)
    }
  }

  if (!semesterId) {
    return (
      <Card>
        <Card.Title style={{ padding: '5%' }}><h2>Kein Semester ausgewählt.</h2></Card.Title>
      </Card>
    );
  } else {
    if (!semester) {
      return (
        <Card>
          <Card.Title style={{ padding: '5%' }}><h3>Semester wird geladen.</h3></Card.Title>
        </Card>
      );
    } else {
      return (
        <>
          <Card>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroupItem>
                  <b>Semester:</b> {semester.name}
                </ListGroupItem>
                <ListGroupItem>
                  <b>ID:</b> {semester.id}
                </ListGroupItem>
                <ListGroupItem>
                  <b>Startjahr:</b> {semester.startYear}
                </ListGroupItem>
                <ListGroupItem>
                  <b>Endjahr:</b> {semester.endYear}
                </ListGroupItem>
              </ListGroup>
            </Card.Body>
            <Card.Footer>
              <Button size="sm" variant="secondary" onClick={() => setEditOpen(true)}>
                Semester bearbeiten
              </Button>
              <Button size="sm" variant="danger" style={{ margin: 3 }} onClick={() => setDeleteOpen(true)}>
                Semester löschen
              </Button>
            </Card.Footer>
          </Card>
          <DeleteSemesterModal
            semesterId={semesterId}
            isOpen={deleteOpen}
            onClose={() => setDeleteOpen(false)}
            onDeleted={() => {
              setDeleteOpen(false);
              if (onDeleted) onDeleted();
            }}
          />
          <EditSemesterModal
            semesterId={semesterId}
            name={semester.name}
            startYear={semester.startYear}
            endYear={semester.endYear}
            isOpen={editOpen}
            onClose={() => setEditOpen(false)}
            onEdited={() => {
              setEditOpen(false);
              reload();
              if (onUpdated) onUpdated();
            }}
          />
        </>
      );
    }
  }
}
