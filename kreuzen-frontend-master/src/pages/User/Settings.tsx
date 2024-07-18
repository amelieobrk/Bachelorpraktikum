import React, { useState } from 'react';
import { Button, Card, Col, Row } from 'react-bootstrap';
import api from "../../api";
import styled from "styled-components";
import GeneralInformation from "../../components/UserSettings/GeneralInformation";
import UpdatePassword from "../../components/UserSettings/UpdatePassword";
import SetMajor from "../../components/UserSettings/SetMajor";
import SetMajorSection from "../../components/UserSettings/SetMajorSection";
import ConfirmDeleteModal from "../../components/UserSettings/ConfirmDeleteModal";
import { useMajor } from "../../hooks/useMajor";
import { useMajorSection } from "../../hooks/useMajorSection";

const Spacer = styled.div`
  height: 24px;
  width: 100%;
`;

interface SettingsProps {
  userId: number
  universityId: number
  username: string
  email: string
  firstName: string
  lastName: string
  reloadAuth: () => void
  logout: () => void
}

/**
 * User Settings for general settings, password changed, major and major section selection.
 */
export default function UserSettings(props: SettingsProps) {

  const { userId, email, universityId, username, firstName, lastName, reloadAuth, logout } = props;

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleteError, setDeleteError] = useState<null | string>(null);

  const {
    availableMajors,
    userMajorIds,
    addMajor,
    removeMajor,
    loading: majorsLoading,
  } = useMajor({
    userId,
    universityId
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

  const confirmDeleteAccount = (u: string) => {
    if (u.toLowerCase() === username.toLowerCase()) {
      api.user.deleteUserById(userId).then(() => {
        setDeleteOpen(false);
        logout();
      })
    } else {
      setDeleteError("Die Nutzernamen stimmen nicht überein!");
    }
  }

  return (
    <Card>
      <Card.Body>
        <Card.Title><h1>Deine Nutzereinstellungen</h1></Card.Title>
        <Row>
          <Col>
            <Card.Title><h2>Name</h2></Card.Title>
            <GeneralInformation
              userId={userId}
              username={username}
              firstName={firstName}
              lastName={lastName}
              reloadAuth={reloadAuth}
              email={email}
              canEditEmail={false}
            />

            <Spacer />
          </Col>
          <Col>
            <Card.Title><h2>Passwort ändern</h2></Card.Title>
            <UpdatePassword userId={userId} />

            <Spacer />
          </Col>
        </Row>
        <Row>
          <Col>
            <Card.Title><h2>Studiengang</h2></Card.Title>
            <SetMajor
              availableMajors={availableMajors}
              majorIds={userMajorIds}
              loaded={!majorsLoading}
              onAddMajor={addMajor}
              onRemoveMajor={removeMajor}
            />

            <Spacer />
          </Col>
          <Col>
            <Card.Title><h2>Studienabschnitt</h2></Card.Title>
            <SetMajorSection
              availableMajors={availableMajors}
              availableSections={availableMajorSections}
              sectionIds={userMajorSectionIds}
              majorIds={userMajorIds}
              onRemoveSection={removeMajorSection}
              onAddSection={addMajorSection}
              loaded={!majorSectionsLoading}
            />

            <Spacer />
          </Col>
        </Row>

        <Button variant="danger" onClick={() => setDeleteOpen(true)}>Account löschen</Button>

        <ConfirmDeleteModal open={deleteOpen} error={deleteError} onClose={() => setDeleteOpen(false)} onConfirm={confirmDeleteAccount} />

      </Card.Body>
    </Card>
  );
}
