import React, { ChangeEvent, useCallback, useEffect, useState } from 'react';
import { Button, Card, Form, Row, Col } from 'react-bootstrap';
import SessionList from "../../../components/Sessions/SessionList";
import { Link } from "react-router-dom";
import { Session, Pagination } from "../../../api/session";
import api from "../../../api";
import { PAGE_SIZE } from "../../../api/user";
import CardHeader from "../../../components/General/CardHeader";

/**
 * Page to list all sessions by the current user.
 */
export default function SessionsOverview(props: { userId: number }) {

  const { userId } = props;

  const [searchOpen, setSearchOpen] = useState(false);
  const [searchString, setSearchString] = useState('');
  const [sessions, setSessions] = useState<Session[]>([]);
  const [count, setCount] = useState(0)
  const [page, setPage] = useState(0)

  // Show pagination when more than one page of sessions available

  const maxPage: number = Math.floor(count / PAGE_SIZE);

  const getPageButtons = (): number[] => {
    const pages: number[] = [];
    if (page - 1 >= 0) {
      pages.push(page - 1);
    }
    pages.push(page);
    if (page + 1 <= maxPage) {
      pages.push(page + 1)
    }
    return pages;
  }

  const nextPage = () => {
    setPage(p => p + 1);
    setSessions([]);
  }

  const prevPage = () => {
    setPage(p => p - 1);
    setSessions([]);
  }

  const jumpPage = (p: number) => {
    setPage(p);
    setSessions([]);
  }

  const changeSearchString = (e: ChangeEvent<HTMLInputElement>) => {
    setSearchString(e.target.value)
  }

  const toggleSearch = () => {
    setSearchString('');
    setSearchOpen(o => !o);
  }

  // set Pagination when loading data 

  const loadData = useCallback(() => {
    api.session.loadSessionsPaginated(userId, page, searchString).then((x: Pagination<Session>) => {
      setCount(x.count);
      setSessions(x.entities);
      // Check whether page is empty
      if (x.count < page * PAGE_SIZE) {
        setPage(Math.floor((x.count - 1) / PAGE_SIZE))
      }
    }).catch(() => { })
  }, [page, searchString, userId]);

  useEffect(loadData, [loadData])

  return (
    <>
      {
        searchOpen && (
          <Card style={{ marginBottom: 32 }}>
            <Card.Body>
              <CardHeader text="Search" secondary />
              <Form.Group>
                <Form.Control
                  name="search"
                  value={searchString}
                  onChange={changeSearchString}
                  placeholder="Search..."
                />
              </Form.Group>
            </Card.Body>
          </Card>
        )
      }
      <Card>
        <Card.Body>
          <CardHeader
            text="Deine Sessions"
            actions={
              (
                <>
                  <Button style={{ marginRight: 8 }} variant="success" as={Link} to="/user/sessions/new">+ Erstellen</Button>
                  <Button onClick={toggleSearch}>Suchen</Button>
                </>
              )
            }
          />
          <SessionList sessions={sessions} onChanged={loadData} />
          {
            maxPage > 1 ? (
              <Row style={{ marginTop: 20 }}>
                <Col>
                  <Button block disabled={page === 0} onClick={prevPage}>
                    Letzte Seite
              </Button>
                </Col>

                <Col style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  {
                    getPageButtons().map(p => (
                      <Button key={p} disabled={p === page} style={{ marginLeft: 8, marginRight: 8 }} onClick={() => jumpPage(p)}>
                        {p + 1}
                      </Button>
                    ))
                  }
                </Col>

                <Col>
                  <Button block disabled={page === maxPage} onClick={nextPage}>
                    Nächste Seite
              </Button>
                </Col>
              </Row>)
              : (<></>)
          }
        </Card.Body>

      </Card>
    </>
  )
}