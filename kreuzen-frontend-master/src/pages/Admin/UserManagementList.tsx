import React, { ChangeEvent, useEffect, useState } from 'react';
import { UserBase, PAGE_SIZE, Pagination } from "../../api/user";
import api from "../../api";
import { Card, Col, ListGroup, Row, Button, Form } from "react-bootstrap";
import { Link } from "react-router-dom";
import axios, { Canceler } from 'axios';
import CardHeader from "../../components/General/CardHeader";

let cancelToken: Canceler | undefined;

/**
 * List of all users including search capabilities.
 */
export default function UserManagementList() {

  const [users, setUsers] = useState<UserBase[]>([]);
  const [page, setPage] = useState(0);
  const [count, setCount] = useState(0);

  const [searchOpen, setSearchOpen] = useState(false);
  const [searchString, setSearchString] = useState('');

  useEffect(() => {
    cancelToken && cancelToken(); // Cancel old requests..
    api.user.loadListPaginated(page, searchString, new axios.CancelToken((c) => {
      cancelToken = c
    })).then((x: Pagination<UserBase>) => {
      setCount(x.count);
      setUsers(x.entities);
      // Check whether page is empty
      if (x.count < page * PAGE_SIZE) {
        setPage(Math.floor((x.count - 1) / PAGE_SIZE))
      }
    }).catch(() => { })
  }, [page, searchString])

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
    setUsers([]);
  }

  const prevPage = () => {
    setPage(p => p - 1);
    setUsers([]);
  }

  const jumpPage = (p: number) => {
    setPage(p);
    setUsers([]);
  }

  const toggleSearch = () => {
    setSearchString('');
    setSearchOpen(o => !o);
  }

  const changeSearchString = (e: ChangeEvent<HTMLInputElement>) => {
    setSearchString(e.target.value)
  }

  const getUserClassName = (u: UserBase): string => {
    if (u.locked) {
      return 'text-danger';
    } else if (!u.emailConfirmed) {
      return 'text-secondary';
    } else {
      return 'text-primary';
    }
  }

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
            text="Nutzerverwaltung"
            actions={<Button onClick={toggleSearch}>Suchen</Button>}
          />

          <ListGroup variant="flush">
            {
              users.map(user => (
                <ListGroup.Item key={user.id}>
                  <Link to={`/admin/users/${user.id}`} className={getUserClassName(user)} >
                    <Row>
                      <Col>
                        {user.firstName} {user.lastName} ({user.username})
                      </Col>
                      <Col>
                        &lt;{user.email}&gt;
                      </Col>
                    </Row>
                  </Link>
                </ListGroup.Item>
              ))
            }
          </ListGroup>

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
          </Row>

        </Card.Body>
      </Card>
    </>
  );
}
