import React, { useEffect, useState } from 'react';
import { Card, Col, ListGroup, Row } from 'react-bootstrap';
import { Module } from "../../api/modules";
import api from '../../api';
import TagCard from "../../components/Tags/TagCard";
import CardHeader from "../../components/General/CardHeader";
import { sortByName } from '../../utils';

export default function ListTags() {
  const [mod, setMod] = useState<Module[]>([])

  // Different attributes for selected module, set Module Id when selected to load Tags on TagCard
  const [selectedId, setSelectedId] = useState(0);
  const [selectedName, setSelectedName] = useState("");
  const [error, setError] = useState<string | null>(null);

  // List the modules when loading the page for the first time
  useEffect(() => {
    api.module.getAllModules().then(
      (res: any) => {
        setMod(res);
      }).catch((err) => {
        setError(err.response?.data?.msg || "Ein Fehler ist aufgetreten")
      })
  }, [])

  return (
    <>
      <Card>
        <Card.Body>
          <CardHeader text="Verwaltung der Tags" />
          {
            error
          }
          <Row>
            <Col className="col-sm-3">
              <h2>Zugehörige Module:</h2>
              <ListGroup variant="flush" style={{ width: 270, height: 500, overflow: "auto", marginTop: 15 }}>
                {mod.sort(sortByName).map((element: Module) =>
                  <ListGroup.Item key={element.id} action={true} onClick={() => {
                    setSelectedId(element.id);
                    setSelectedName(element.name);
                  }}>
                    {element.id === selectedId ? <b>{element.name}</b> : element.name}
                  </ListGroup.Item>
                )}
              </ListGroup>
            </Col>
            <Col>
              <TagCard moduleId={selectedId} moduleName={selectedName} onCreated={() => { }} onUpdated={() => { }} />
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </>
  );
}
