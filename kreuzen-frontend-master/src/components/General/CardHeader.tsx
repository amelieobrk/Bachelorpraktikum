import React, {ReactElement} from 'react';
import { Card} from "react-bootstrap";
import styled from "styled-components";

const CardTitle = styled(Card.Title)`
  margin-bottom: 32px;
  display: flex;
  flex: 1;
  justify-content: space-between;
  align-items: center;
`;

/**
 * Header for a card.
 */
export default function CardHeader(props: {text: string, actions?: ReactElement, secondary?: boolean}) {

  const {text, actions, secondary} = props;

  return (
    <CardTitle>
      {
        secondary ? (
          <h3>
            {text}
          </h3>
        ) : (
          <h1>
            {text}
          </h1>
        )
      }
      <div>
        { actions }
      </div>
    </CardTitle>
  );
}
