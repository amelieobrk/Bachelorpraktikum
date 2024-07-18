import React, { useEffect, useState } from 'react';
import { Button, Card, Col, ListGroup, Row } from 'react-bootstrap';
import { Module } from "../../api/modules";
import api from '../../api';

import ModuleCard from "../../components/Module/ModuleCard";
import CreateModuleModal from "../../components/Module/CreateModuleModal";
import { sortByName } from '../../utils';

/**
 * Page listing all modules.
 */
export default function ListModules() {
  const [mod, setMod] = useState<Module[]>([])

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [createOpen, setCreateOpen] = useState(false);

  // Update the List of displayed Semesters
  function reloadModules() {
    api.module.getAllModules()
      .then((res: Module[]) => {
        setMod(res);
      })
  }

  // List the modules when loading the page for the first time
  useEffect(() => {
    api.module.getAllModules().then(
      (res: any) => {
        setMod(res);
      }).catch((err) => {
        if (err.response && err.response.data && err.response.data.msg) {
          console.error("Could not load modules!")
        }
      })
  }, [])

  return (
    <>
      <Card>
        <Card.Body>
          <Card.Title><h1>Modulverwaltung</h1></Card.Title>
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
                <div className="scrollbar scrollbar-juicy-peach">
              <ListGroup variant="flush" style={{width: 270, height: 420, overflow: "auto"}}>
                {mod.sort(sortByName).map((element: Module) =>
                  <ListGroup.Item key={element.id} action={true} onClick={() => setSelectedId(element.id)}>
                    {selectedId === element.id ? <b>{element.name}</b> : element.name}
                  </ListGroup.Item>
                )}
              </ListGroup>
              </div>
            </Col>
            <Col>
              <ModuleCard moduleId={selectedId} onUpdated={() => reloadModules()} onDeleted={() => {
                setSelectedId(null);
                reloadModules()
              }} />
            </Col>
          </Row>
        </Card.Body>
      </Card>
      <CreateModuleModal
        isOpen={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={() => {
          setCreateOpen(false);
          reloadModules();
        }}
      />
    </>
  );
}
