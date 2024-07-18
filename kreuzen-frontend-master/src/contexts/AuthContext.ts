import React from 'react';
import {UserDetails} from "../api/auth";

interface AuthContextType {
  token: string | null
  user: UserDetails | null
  isInit: boolean
  init: () => void
  logout: () => void
  reloadAuth: () => void
}

const AuthContext = React.createContext<AuthContextType>({
  token: null,
  user: null,
  isInit: false,
  init: () => {},
  logout: () => {},
  reloadAuth: () => {}
});

export default AuthContext;
