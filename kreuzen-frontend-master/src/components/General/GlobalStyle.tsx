import { createGlobalStyle } from 'styled-components';

const GlobalStyle = createGlobalStyle`
body{
  margin: 0;
  padding: 1;
  font-family: Open-Sans, Helvetica, Sans-Serif;
  background: #ebf7ff;
  color.primary: black;
  h1 {vertical-align: left; text-align: left; color:#223d7b ; font-family: 'Open Sans Condensed', sans-serif; font-size: 35px; font-weight: 700; line-height: 64px; margin: 0 0 0; padding: 20px 30px;}
  h2 {vertical-align: left; text-align: left; color:#223d7b ; font-family: 'Open Sans Condensed', sans-serif; font-size: 20px}
  h3 {vertical-align: middle; color:#223d7b ; font-family: 'Open Sans Condensed', sans-serif; font-size: 15px; font-weight: 700; line-height: 35px; margin: 0 0 0}
  Jumbotron {margin-bottom: 0px;}
}

h1 {
  font-weight: 600;
  font-size: 1.7rem;
}

`;

export default GlobalStyle;
