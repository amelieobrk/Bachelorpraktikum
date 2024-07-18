import React, {ChangeEvent} from 'react';
import {Form} from 'react-bootstrap';
import {Major} from "../../api/university";

interface SetMajorProps {
  availableMajors: Major[]
  majorIds: number[]
  loaded: boolean
  onAddMajor: (id: number) => void
  onRemoveMajor: (id: number) => void
}

/**
 * Form to select majors
 */
export default function SetMajor(props : SetMajorProps) {

  const {availableMajors, majorIds, loaded, onAddMajor, onRemoveMajor} = props;

  const toggleMajor = (e : ChangeEvent<HTMLInputElement>) => {

    const id : number | null = Number.parseInt(e.target.id);

    if (id != null) {
      if (majorIds.includes(id)) {
        onRemoveMajor(id)
      } else {
        onAddMajor(id)
      }
    }
  }

  return (
    <>
      {
        loaded && availableMajors.map(major => <Form.Check
          type="checkbox"
          key={major.id}
          id={String(major.id)}
          data-testid={String(major.id)}
          label={major.name}
          checked={majorIds.includes(major.id)}
          onChange={toggleMajor}
        />)
      }
    </>
  );
}
