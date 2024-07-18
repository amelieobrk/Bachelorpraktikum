import React from 'react';
import { Toast } from 'react-bootstrap';
import styled from "styled-components";

const Wrapper = styled.div`
  position: fixed;
  right: 10px;
  bottom: 10px;
`

interface NotificationProps {
  onClose : () => void
  open: boolean
  text: string | null | undefined
  header: string | null | undefined
}

/**
 * Notification that automatically disappears
 *
 * @param props
 */
export default function Notification(props : NotificationProps) {
  const { open, onClose, text, header } = props;

  return (
    <Wrapper>
      <Toast onClose={onClose} show={open} delay={3000} autohide>
        <Toast.Header>
          <strong className="mr-auto">{header}</strong>
        </Toast.Header>
        <Toast.Body>{text}</Toast.Body>
      </Toast>
    </Wrapper>
  );
}
