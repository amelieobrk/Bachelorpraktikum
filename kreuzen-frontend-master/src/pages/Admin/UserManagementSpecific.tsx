import React, {useEffect, useState} from 'react';
import {UserBase} from "../../api/user";
import api from "../../api";
import {Card, Button} from "react-bootstrap";
import GeneralInformation from "../../components/UserSettings/GeneralInformation";
import styled from "styled-components";
import SetMajor from "../../components/UserSettings/SetMajor";
import SetMajorSection from "../../components/UserSettings/SetMajorSection";
import {Link, useHistory} from "react-router-dom"
import ConfirmDeleteModal from "../../components/UserSettings/ConfirmDeleteModal";
import SetRole from "../../components/UserSettings/SetRole";
import AdminUpdatePassword from "../../components/UserSettings/AdminUpdatePassword";
import { prettyPrintDateTime} from "../../utils";
import {useMajor} from "../../hooks/useMajor";
import {useMajorSection} from "../../hooks/useMajorSection";
import CardHeader from "../../components/General/CardHeader";

interface UserManagementSpecificParams {
  userId: number
}

const Spacer = styled.div`
  height: 24px;
  width: 100%;
`;

/**
 * Admin panel to edit a specific user.
 */
export default function UserManagementSpecific(params : UserManagementSpecificParams) {

  const {userId} = params;

  const [user, setUser] = useState<UserBase | null>(null);

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleteError, setDeleteError] = useState<null | string>(null);

  const history = useHistory();

  const {
    availableMajors,
    userMajorIds,
    addMajor,
    removeMajor,
    loading: majorsLoading,
  } = useMajor({
    userId,
    universityId: user?.universityId
  });

  const {
    availableMajorSections,
    userMajorSectionIds,
    addMajorSection,
    removeMajorSection,
    loading: majorSectionsLoading
  } = useMajorSection({
    userId,
    userMajorIds
  })

  // Load User
  useEffect(() => {
    api.user.getUserById(userId).then((u: UserBase) => setUser(u))
  }, [userId])

  const resendConfirmationMail = () => {
    if (user?.email) {
      api.auth.resendConfirmationMail(user.email).then(() => {
        api.user.getUserById(userId).then((u: UserBase) => setUser(u));
      });
    }
  }

  const confirmDeleteAccount = (u: string) => {
    if (u.toLowerCase() === user?.username?.toLowerCase()) {
      api.user.deleteUserById(userId).then(() => {
        setDeleteOpen(false);
        history.push("/Admin/users")
      })
    } else {
      setDeleteError("Die Nutzernamen stimmen nicht überein!");
    }
  }

  const confirmAccount = () => {
    api.auth.adminConfirmMail(userId).then(() => {
      api.user.getUserById(userId).then((u: UserBase) => setUser(u))
    })
  }

  const lockAccount = (locked : boolean) => {
    api.user.setUserLocked(userId, locked).then(() => {
      api.user.getUserById(userId).then((u: UserBase) => setUser(u))
    })
  }

  return (
    <Card>
      <Card.Body>
        {user && (
          <>

            <CardHeader
              text="Allgemeine Informationen"
              actions={
                <Button variant="secondary" as={Link} to="/admin/users">
                  <i className="fas fa-arrow-left" />
                </Button>
              }
            />

            <Card.Text>
              <b>ID:</b> {user.id}<br/>
              <b>Email:</b> {user.email}<br/>
              <b>Email bestätigt:</b> {user.emailConfirmed ? "ja" : "nein"}<br/>
              <b>Gesperrt:</b> {user.locked ? "ja" : "nein"}<br/>
              <b>Registriert am:</b> {prettyPrintDateTime(user.createdAt)}<br/>
              <b>Letzte Accountänderung:</b> {prettyPrintDateTime(user.updatedAt)}
            </Card.Text>
            <GeneralInformation
              userId={user.id}
              username={user.username}
              firstName={user.firstName}
              lastName={user.lastName}
              reloadAuth={() => api.user.getUserById(userId).then((u : UserBase) => setUser(u))}
              canEditEmail={!user.emailConfirmed}
              email={user.email}
            />

            <Spacer/>
            <CardHeader text="Passwort" secondary />
            <AdminUpdatePassword userId={userId} />

            <Spacer/>

            <CardHeader text="Studiengang" secondary />
            <SetMajor
              availableMajors={availableMajors}
              majorIds={userMajorIds}
              loaded={!majorsLoading}
              onAddMajor={addMajor}
              onRemoveMajor={removeMajor}
            />

            <Spacer/>

            <CardHeader text="Studienabschnitt" secondary />
            <SetMajorSection
              availableMajors={availableMajors}
              availableSections={availableMajorSections}
              sectionIds={userMajorSectionIds}
              majorIds={userMajorIds}
              onRemoveSection={removeMajorSection}
              onAddSection={addMajorSection}
              loaded={!majorSectionsLoading}
            />

            <Spacer/>

            <CardHeader text="Rolle" secondary />
            <SetRole userId={user.id} role={user.role} reloadUser={() => api.user.getUserById(userId).then((u: UserBase) => setUser(u))} />

            <Spacer/>

            <CardHeader text="Admin" secondary />
            <Button variant="danger" onClick={() => setDeleteOpen(true)} style={{marginRight: 8, marginBottom: 8}}>Löschen</Button>
            {
              user.locked ? (
                <Button variant="danger" onClick={() => lockAccount(false)} style={{marginRight: 8, marginBottom: 8}}>Entsperren</Button>
              ) : (
                <Button variant="danger" onClick={() => lockAccount(true)} style={{marginRight: 8, marginBottom: 8}}>Sperren</Button>
              )
            }
            {
              !user.emailConfirmed && (
                <>
                  <Button variant="warning" onClick={confirmAccount} style={{marginRight: 8, marginBottom: 8}}>Email bestätigen</Button>
                  <Button variant="primary" onClick={resendConfirmationMail} style={{marginRight: 8, marginBottom: 8}}>Aktivierungs-Email erneut senden</Button>
                </>
              )
            }

            <ConfirmDeleteModal open={deleteOpen} error={deleteError} onClose={() => setDeleteOpen(false)} onConfirm={confirmDeleteAccount} />

          </>
        )}

      </Card.Body>
    </Card>
  );
}
