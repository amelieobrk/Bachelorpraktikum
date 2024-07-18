import React from 'react';
import { Container } from 'react-bootstrap';
import styled from "styled-components";

interface MainLayoutProps {
  children : any
}

const Wrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
`

const StyledContainer = styled(Container)`
  width: 100%;
  max-width: 500px;
  padding: 16px;
`

/**
 * Layout that is displayed to users who are unauthenticated.
 * It puts everything in the center of the screen and limits the width by 500px.
 *
 * @param props
 * @constructor
 */
function UnauthenticatedLayout(props : MainLayoutProps) {
  return (
    <Wrapper>
      <StyledContainer>
        {props.children}
      </StyledContainer>
    </Wrapper>
  );
}

export default UnauthenticatedLayout;
