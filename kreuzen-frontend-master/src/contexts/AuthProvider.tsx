import React, { useCallback, useEffect, useState } from 'react';
import Cookies from 'js-cookie';
import { BroadcastChannel } from 'broadcast-channel';
import AuthContext from './AuthContext';
import api from '../api';
import {UserDetails} from "../api/auth";

declare type LoginMessage = {
  token: string;
};
declare type LogoutMessage = {};

const loginChannel: BroadcastChannel<LoginMessage> = new BroadcastChannel('login');
const logoutChannel: BroadcastChannel<LogoutMessage> = new BroadcastChannel('logout');

interface AuthProviderProps {
  children : any
}

const AUTH_COOKIE : string = 'auth-token';

/**
 * Provider for the AuthContext.
 * It sets up an response interceptor to extract authentication headers from the server. Further automatic logout
 * is performed if the token is invalid.
 *
 * @param props
 */
const AuthProvider = (props : AuthProviderProps) => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<UserDetails | null>(null);
  const [isInit, setIsInit] = useState<boolean>(false);

  const logout = useCallback((informOthers : boolean = true) => {
    setIsInit(true);
    setToken(null);
    setUser(null);
    removeAuthTokenCookie();
    api.removeAuthorization();
    if (informOthers) {
      logoutChannel.postMessage({});
    }
  }, []);

  const init = () : void => {
    // Add axios interceptor for authorization header
    api.axiosInstance.interceptors.response.use((response) => {
      if (response.headers["authorization"]) {
        setAuthToken(response.headers["authorization"]);
      }
      return response;
    })

    // Check for session auth
    const t : (string | null) = getAuthTokenCookie();
    if (t) {
      setAuthToken(t, false);
      // Set is init after loading /auth/me
    } else {
      setIsInit(true);
    }

    loginChannel.onmessage = (msg : LoginMessage) => setAuthToken(msg.token, false);
    logoutChannel.onmessage = () => logout(false);
  };

  const loadCurrentUser = useCallback(() => {
    // load user details
    api.auth.getUserDetails().then((user) => {
      setUser(user);
      setIsInit(true);
    }).catch(() => logout());
  }, [logout]);

  const setAuthToken = useCallback((t : string, informOthers : boolean = true) : void => {
    // set token in api
    api.setAuthorization(t);

    // store token as cookie
    setAuthTokenCookie(t);

    loadCurrentUser();

    setToken(t);

    if (informOthers) {
      loginChannel.postMessage({
        token: t,
      });
    }
  }, [loadCurrentUser]);

  const setAuthTokenCookie = (t : string) : void => {
    Cookies.set(AUTH_COOKIE, t);
  };

  const removeAuthTokenCookie = () : void => {
    Cookies.remove(AUTH_COOKIE);
  };

  const getAuthTokenCookie = () : (string | null) => Cookies.get()[AUTH_COOKIE];

  // When the app is opened, init is called once.
  useEffect(init, [logout, setAuthToken]);

  const { children } = props;

  return (
    <AuthContext.Provider value={{
      token,
      user,
      isInit,
      init,
      logout,
      reloadAuth: loadCurrentUser
    }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export default AuthProvider;
