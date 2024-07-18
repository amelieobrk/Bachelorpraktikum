import React, {useCallback, useEffect, useState} from 'react';
import {Button, Card, ListGroup} from "react-bootstrap";
import CommentDisplay from './Comment'
import api from "../../../api";
import {Comment} from "../../../api/question";
import AddCommentField from "./AddCommentField";
import CardHeader from "../../General/CardHeader";

/**
 * Card displaying all comments.
 */
export default function CommentBox(props : {questionId: number, userId: number}) {

  const {questionId, userId} = props;

  const [comments, setComments] = useState<Comment[]>([])
  const [isOpen, setIsOpen] = useState(false)

  const loadComments = useCallback(() => {
    if (questionId !== 0) {
      api.question.getComments(questionId).then(setComments)
    }
  }, [questionId])

  useEffect(loadComments, [loadComments])

  return (
    <>
      <Card style={{marginTop: 48}}>
        <Card.Body>
          <CardHeader
            text="Kommentare"
            actions={
              <Button variant="secondary" onClick={() => setIsOpen(x => !x)}>
                <i className={`fas fa-arrow-${isOpen ? 'up' : 'down'}`} />
              </Button>
            }
            secondary
          />

          {
            isOpen && (
              <div style={{marginTop: 32}}>
                <ListGroup>
                  {
                    comments.map(comment => (
                      <ListGroup.Item key={comment.id}>
                        <CommentDisplay userId={userId} comment={comment} onUpdated={loadComments} />
                      </ListGroup.Item>
                    ))
                  }
                </ListGroup>

                <AddCommentField questionId={questionId} onEntered={loadComments} />
              </div>
            )
          }

        </Card.Body>
      </Card>
    </>
  )
}
