import React, {useEffect, useState} from 'react';
import {Form} from 'react-bootstrap';
import api from "../../api";
import {Role} from "../../api/user";

interface SetRoleProps {
  userId: number
  role: string
  reloadUser: () => void
}

/**
 * Form to update role
 */
export default function SetRole(props : SetRoleProps) {

  const {userId, role, reloadUser} = props;

  const [roles, setRoles] = useState<Role[]>([]);

  useEffect(() => {
    api.role.getAllRoles().then(setRoles)
  }, [])

  const setUserRole = (r : string) => {
    api.user.setUserRole(userId, r).then(() => {
      reloadUser();
    })
  }

  return (
    <Form.Group>
      {
        roles.map(r => <Form.Check
          key={r.name}
          type="radio"
          label={r.displayName}
          name={r.name}
          id={r.name}
          onChange={() => setUserRole(r.name)}
          checked={r.name === role}
        />)
      }
    </Form.Group>
  );
}
