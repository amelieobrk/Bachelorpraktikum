import { AxiosInstance } from 'axios';



export interface UserDetails {
  id: number
  username: string
  email: string
  role: string
  universityId: number
  firstName: string
  lastName: string
  createdAt: Date
  updatedAt: Date
}

export interface University {
  name: string,
  id: number,
  allowedMailDomains: string[]
}

export interface PreRegistration {
  universities: University[]
}

/**
 * This class provides methods to access the backend api.
 */
export default class AuthApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Logs a user in. The auth token is not supplied, as it is intercepted by the AuthProvider.
   *
   * @param username
   * @param password
   */
  login(username: string, password: string): Promise<void> {
    return this.axios.post('/auth/login', { username, password })
      .then(() => {});
  }

  /**
   * Get information about the currently logged in user
   */
  getUserDetails(): Promise<UserDetails> {
    return this.axios.get('/auth/me')
      .then((response) => {
        const state: UserDetails = {
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          role: response.data.role,
          universityId: response.data.universityId,
          firstName: response.data.firstName,
          lastName: response.data.lastName,
          createdAt: new Date(response.data.createdAt),
          updatedAt: new Date(response.data.updatedAt)
        };
        return state;
      });
  }

  /**
   * Performs a pre registration check for the username and email. Further it provides a list of possible universities.
   *
   * @param username
   * @param email
   */
  preRegister(username: string, email: string): Promise<PreRegistration> {

    return this.axios.get(`/auth/pre-register?username=${username}&email=${email}`)
      .then(res => {
        const ret : PreRegistration = {
          universities: res.data.universities.map((u: University) => {
            const uni : University = {
              id: u.id,
              name: u.name,
              allowedMailDomains: u.allowedMailDomains
            };
            return uni;
          })
        }
        return ret;
      })
  }

  /**
   * Registers a new user.
   *
   * @param firstName
   * @param lastName
   * @param username
   * @param email
   * @param password
   * @param universityId
   * @param majors
   * @param majorSections
   */
  register(firstName: string, lastName: string, username: string, email: string, password: string, universityId: number, majors: number[], majorSections: number[]): Promise<void> {
    return this.axios.post('/auth/register', {
      firstName,
      lastName,
      username,
      email,
      password,
      universityId,
      majors,
      majorSections
    }).then(() => {});
  }

  /**
   * Confirms an account. Automatic auth should be performed by the AuthProvider interceptor.
   *
   * @param token
   */
  confirmAccount(token: string): Promise<void> {
    return this.axios.post('/auth/confirm-email', {
      token,
    }).then(() => {})
  }

  /**
   * Requests a password reset for the given user.
   *
   * @param email
   */
  requestPasswordReset(email: string) : Promise<void> {
    return this.axios.post('/auth/request-pw-reset', {email}).then(() => {});
  }

  /**
   * Confirms a password reset using a token. Further the new password is set.
   *
   * @param token
   * @param newPassword
   */
  confirmPasswordReset(token: string, newPassword: string) : Promise<void> {
    return this.axios.post('/auth/confirm-pw-reset', {token, newPassword}).then(() => {});
  }

  /**
   * Requests that a confirmation mail is sent again.
   * @param email
   */
  resendConfirmationMail(email: string) : Promise<void> {
    return this.axios.post('/auth/resend-confirmation-email', {
      email
    })
  }

  /**
   * Confirms the email of the given user.
   * @param userId
   */
  adminConfirmMail(userId: number) : Promise<void> {
    return this.axios.post('/auth/confirm-email-Admin', {userId})
  }
}
