import React, { useEffect, useState } from 'react';
import { Button, Card, ListGroup} from 'react-bootstrap';
import { Tag } from "../../api/tags";
import api from '../../api';
import CreateTagModal from "./CreateTagModal";
import EditTagModal from "./EditTagModal";
import { sortByName } from '../../utils';

/**
 * Card displaying the tags of a selected module and options to delete, create and edit it.
 */
export default function ModuleCard(props : {
  moduleId: number | null | undefined,
  moduleName: String,
  onUpdated: (() => void) | undefined | null,
  onCreated: (() => void) | undefined | null
}) {

  const {moduleId, onUpdated, onCreated} = props;

  const [tags, setTags] = useState<Tag[]>([]);
  const [tag, setTag] = useState<Tag|null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [selectedTagId, setSelectedTagId] = useState(0);

//Load tags when associated module selected
  useEffect(() => {
    if (moduleId) {
      api.tag
        .getTags(moduleId)
        .then(setTags)
    }
    setTag(null)
  }, [moduleId])

/*
** Reload list of all tags when tag edited
*/
  const reload = (moduleId: number) => {
    if (tag?.id) {
      api.tag.getTagById(tag.id).then(setTag)
      api.tag.getTags(moduleId).then(setTags)
    }
  }

  /*
** Reload list of all tags when new tag deleted or new tag created 
*/

  const reloadTags = ( moduleId: number) => {
    api.tag.getTags(moduleId)
    .then(( res: any ) => setTags(res))
  }
  
  
  if (!moduleId) {
    return (
      <Card>
        <Card.Title style={{ padding: '5%' }}><h2>Kein Modul ausgewählt.</h2></Card.Title>
      </Card>
    );
  } else {
      return (
        <>
          <Card>
            <Card.Body>
              <Card.Title>Tags des Moduls {props.moduleName}</Card.Title>
              <ListGroup variant="flush" style={{width: 750, height: 420, overflow: "auto"}}>
                {
                tags.sort(sortByName).map((element: Tag) =>
                  <ListGroup.Item key={element.id} action={true} onClick={() => {
                    setTag(element);
                    setSelectedTagId(element.id);
                  }}>
                    {element.id === tag?.id ? <b>{element.name}</b> : element.name}
                  </ListGroup.Item>
                )}
              </ListGroup>
            </Card.Body>    
            <Card.Footer>
              <Button
                onClick={() => setCreateOpen(true)}
                size="sm"
                variant="primary"
                style={{ margin: 3 }}
              >
                Tag hinzufügen
              </Button>
              <Button
                onClick={() => setEditOpen(true)}
                size="sm"
                variant="secondary"
                style={{ margin: 3 }}
                disabled={!tag}
              >
                Tag bearbeiten
              </Button>
              <Button
                variant="danger"
                size="sm"
                onClick={()=> {
                  api.tag.deleteTag(selectedTagId)
                    .then(() => reloadTags(moduleId))
                    .catch(() => console.log("Fehler beim löschen"))
                }}
                disabled={!tag}
              >
                    Tag löschen
              </Button>
            </Card.Footer>
          </Card>
          <CreateTagModal
            moduleId={moduleId}
            isOpen={createOpen}
            onClose={() => setCreateOpen(false)}
            onCreated={() => {
              reloadTags(moduleId)
              setCreateOpen(false);
              if (onCreated) onCreated();
            }}
          />
          <EditTagModal
            tagId = {selectedTagId}
            tagName={tag?.name||""}
            isOpen={editOpen}
            onClose={() => setEditOpen(false)}
            onEdited={() => {
              reload(moduleId);
              setEditOpen(false);
              if (onUpdated) onUpdated();
            }}
          />
        </>
      );
    }
  }


