import React, {useEffect, useState} from 'react';
import {Button, ListGroup, Modal} from 'react-bootstrap';
import api from '../../api';
import {Tag} from "../../api/tags";


/**
 * Modal to show all tags of a module.
 */
export default function TagListModal(props : {
  moduleId: number | null | undefined,
  isOpen: boolean,
  onClose: () => void
}) {

  const {moduleId, isOpen, onClose} = props;

  const[tags, setTags] = useState<Tag[]>([])

  /**
   * Reload tags when ( another ) module selected 
   */
  useEffect(() => {
    if (moduleId) {
      api.tag.getTags(moduleId).then(setTags)
    } else {
      setTags([])
    }
  }, [moduleId])

  return (
    <Modal show={isOpen} onHide={onClose} transition="false">
      <Modal.Header>
        <b>Tags dieses Moduls</b>
      </Modal.Header>
      <Modal.Body>
      <ListGroup variant="flush" style={{width: 470, height: 420, overflow: "auto"}}>
          {
            tags.map((element: Tag) =>
              <ListGroup.Item key={element.id} action={true}>
                  {element.name}
              </ListGroup.Item>
          )}
        </ListGroup>
        <Modal.Footer>
          <Button onClick={onClose}>Schließen</Button>
        </Modal.Footer>
      </Modal.Body>
    </Modal>
  );
}
