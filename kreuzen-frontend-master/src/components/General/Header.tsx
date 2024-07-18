import React from 'react';
import { Nav, Navbar, NavDropdown, Dropdown } from 'react-bootstrap';
import { Link, NavLink } from 'react-router-dom';
import { UserDetails } from "../../api/auth";
import logo from "../../Grafiken/Logo.png";

interface HeaderProps {
  logout: () => void
  user: UserDetails | null | undefined
}

/**
 * Header that is displayed to a logged in user. Different displays depending on role of user 
 *
 * @param props
 */
export default function Header(props: HeaderProps) {
  const { logout, user } = props;
  // User- and Admin- Navigation
  return (
    <Navbar bg="primary" variant="dark" expand="sm">
      <Navbar.Brand as={Link} to="/">
        <img src={logo} alt="Kreuzen Logo" width="40" height="40" />
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className="mr-auto">
          <Nav.Link as={NavLink} exact to="/">Home</Nav.Link>
          <Nav.Link as={NavLink} to="/user/sessions">Sessions</Nav.Link>
          <Nav.Link as={NavLink} to="/user/questions">Fragen</Nav.Link>
          <Nav.Link as={NavLink} to="/user/stats">Statistiken</Nav.Link>
          {
            (user?.role === "ADMIN" || user?.role === "MOD" || user?.role === "SUDO") && (
              <NavDropdown title="Admin-Verwaltung" id="basic-nav-dropdown">
                <NavDropdown.Item as={NavLink} to="/admin/semester">Semesterverwaltung</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/modules">Modulverwaltung</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/exams">Klausurverwaltung</NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item as={NavLink} to="/admin/users">Benutzerverwaltung</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/tags">Tagverwaltung</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/questions">Fragenverwaltung</NavDropdown.Item>
              </NavDropdown>
            )
          }
        {/*User-Settings, Help and Logout*/}
        </Nav>
        <Nav className="ml-auto">
          <Dropdown>
            <Dropdown.Toggle as={Nav.Link}>
              {user?.username || 'Me'}
            </Dropdown.Toggle>
            <Dropdown.Menu align="right">
              <Dropdown.Item as={NavLink} to="/me/settings">Einstellungen</Dropdown.Item>
              <Dropdown.Item as={NavLink} to="/help">Hilfe</Dropdown.Item>
              <NavDropdown.Divider />
              <Dropdown.Item onClick={logout}>Logout</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>

        </Nav>
      </Navbar.Collapse>
    </Navbar>
  );
}
