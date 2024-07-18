import React from 'react';
import styled from "styled-components";
import {BarLoader} from "react-spinners";

const Wrapper = styled.div`
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
`

const LoadingText = styled.p`
  margin-top: 8px;
`

/**
 * Loading page.
 */
export default function Loading() {

  return (
    <Wrapper>
      <BarLoader height={5} width={200} color="#3778c2" />
      <LoadingText>
        Loading Kreuzen...
      </LoadingText>
    </Wrapper>
  );
}
