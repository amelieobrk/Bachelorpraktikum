import React, { useEffect, useState } from 'react';
import { Button, Card, Col, ListGroup, Row } from 'react-bootstrap';
import { Semester } from "../../api/semester";
import api from '../../api';
import CreateSemesterModal from "../../components/Semester/CreateSemesterModal";
import SemesterCard from "../../components/Semester/SemesterCard";

/**
 * Page listing all semesters.
 */
export default function ListSemesters() {

  const [semesters, setSemesters] = useState<Semester[]>([])
  const [selectedSemesterId, setSelectedSemesterId] = useState<number | null>(null)
  const [createOpen, setCreateOpen] = useState(false);

  // Update the List of displayed Semesters
  function reloadSemesters() {
    api.semester.getSemesters()
      .then((res: any) => {
        setSemesters(res);
      })
  }

  // List the semesters when loading the page for the first time
  useEffect(() => {
    api.semester.getSemesters().then(
      (res: any) => {
        setSemesters(res);
      })
  }, [])

  return (
    <>
      <Card>
        <Card.Body>
          <Card.Title><h1>Semesterverwaltung</h1></Card.Title>{
            <Row>
              <Col className="col-sm-3">
                <Button
                  block
                  style={{ marginTop: 16 }}
                  onClick={() => setCreateOpen(true)}
                  variant="primary"
                >
                  &#43;
                </Button>
                <br/>
                <ListGroup variant="flush" style={{width: 270, height: 420, overflow: "auto"}}>
                  {semesters.sort((a, b) => b.endYear - a.endYear).map((element: Semester) =>
                    <ListGroup.Item key={element.id} action={true} onClick={() => setSelectedSemesterId(element.id)}>
                      {element.name}
                    </ListGroup.Item>
                  )}
                </ListGroup>
              </Col>
              <Col>
                <SemesterCard
                  semesterId={selectedSemesterId}
                  onDeleted={() => {
                    reloadSemesters();
                    setSelectedSemesterId(null);
                  }}
                  onUpdated={reloadSemesters} />
              </Col>
            </Row>
          }
        </Card.Body>
      </Card>
      <CreateSemesterModal isOpen={createOpen} onClose={() => setCreateOpen(false)} onCreated={() => {
        reloadSemesters();
        setSelectedSemesterId(null);
        setCreateOpen(false);
      }} />
    </>
  );
}
