import React from 'react';
import { Container } from 'react-bootstrap';
import Header from '../components/General/Header';
import AuthContext from '../contexts/AuthContext';
import styled from "styled-components";
import Footer from '../components/General/Footer';

interface MainLayoutProps {
  children : any
}

const MainContainer = styled(Container)`
  margin-top: 40px;
  padding-bottom: 40px;
`;

/**
 * Main Layout that is used after a user authenticated themself.
 *
 * @param props
 */
function MainLayout(props : MainLayoutProps) {
  return (
    <AuthContext.Consumer>
      {
        (auth) => (
          <>
            <Header logout={auth.logout} user={auth.user} />
            <MainContainer>
              {props.children}
            </MainContainer>
            <Footer />
          </>
        )
      }
    </AuthContext.Consumer>
  );
}

export default MainLayout;
