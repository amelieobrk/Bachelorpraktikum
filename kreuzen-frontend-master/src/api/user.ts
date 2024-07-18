import { AxiosInstance, CancelToken } from 'axios';
import { Session } from './session';
import { Major } from "./university";

export interface UserBase {
  id: number
  username: string
  firstName: string
  lastName: string
  email: string
  universityId: number
  emailConfirmed: boolean
  locked: boolean
  role: string
  createdAt: Date
  updatedAt: Date
}

export interface Role {
  name: string
  displayName: string
}

export interface Pagination<T> {
  count: number
  entities: T[]
}

export const PAGE_SIZE = 20;

const mapUserResponseToUserBase = (data: any): UserBase => {
  return {
    id: data.id,
    username: data.username,
    firstName: data.firstName,
    lastName: data.lastName,
    email: data.email,
    universityId: data.universityId,
    emailConfirmed: data.emailConfirmed,
    locked: data.locked,
    role: data.role,
    createdAt: new Date(data.createdAt),
    updatedAt: new Date(data.updatedAt)
  }
}

/**
 * Access class to access the user endpoints.
 */
export default class UserApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Gets all majors that a user attends
   *
   * @param userId
   */
  getMajorsByUserId(userId: number): Promise<Major[]> {
    return this.axios.get(`/user/${userId}/major`)
      .then(res => {
        const majors: Major[] = res.data.map((m: Major) => {
          const major: Major = {
            id: m.id,
            name: m.name,
            sections: null
          };
          return major;
        });
        return majors;
      })
  }

  /**
   * Gets all sessions of a User
   *
   * @param id
   */
  getSessionsByUser(id: number): Promise<Session[]> {
    return this.axios.get(`/user/${id}/session`)
      .then(res => {
        const sessions: Session[] = res.data.entities.map((s: Session) => {
          const session: Session = {
            id: s.id,
            name: s.name,
            notes: s.notes,
            type: s.type,
            isRandom: s.isRandom,
            isFinished: s.isFinished,
            createdAt: s.createdAt,
            updatedAt: s.updatedAt
          };
          return session;
        });
        return sessions;
      })
  }

  /**
   * Updates the password of a user
   *
   * @param userId affected user
   * @param newPassword new password
   * @param oldPassword old password for confirmation
   */
  updatePassword(userId: number, newPassword: string, oldPassword: string | null): Promise<void> {
    return this.axios.patch(`/user/${userId}`, { newPassword, oldPassword }).then(() => { });
  }

  /**
   * Updates all non null values of the user.
   *
   * @param userId
   * @param username
   * @param firstName
   * @param lastName
   * @param email
   */
  updateUserData(userId: number, username: string | null, firstName: string | null, lastName: string | null, email: string | null | undefined): Promise<void> {
    return this.axios.patch(`/user/${userId}`, {
      newUsername: username,
      newFirstName: firstName,
      newLastName: lastName,
      newEmail: email
    }).then(() => { });
  }

  /**
   * Loads a paginated list of users. Optionally a search term can be set, which filters users.
   * A cancel token can be given which will be handed to axios.
   *
   * @param page
   * @param searchTerm
   * @param cancelToken
   */
  loadListPaginated(page: number, searchTerm: string | null = null, cancelToken: CancelToken | undefined = undefined): Promise<Pagination<UserBase>> {
    return this.axios.get(
      `/user?limit=${PAGE_SIZE}&skip=${page * PAGE_SIZE}${searchTerm != null ? `&searchTerm=${encodeURIComponent(searchTerm)}` : ''}`,
      { cancelToken })
      .then(res => {
        const page: Pagination<UserBase> = {
          count: res.data.count,
          entities: res.data.entities.map(mapUserResponseToUserBase)
        }
        return page;
      });
  }

  /**
   * Get a specific user by id
   *
   * @param userId
   */
  getUserById(userId: number): Promise<UserBase> {
    return this.axios.get(`/user/${userId}`).then(res => mapUserResponseToUserBase(res.data));
  }

  /**
   * Delete a user by id
   *
   * @param userId
   */
  deleteUserById(userId: number): Promise<void> {
    return this.axios.delete(`/user/${userId}`);
  }

  /**
   * Set the role of a user.
   *
   * @param userId
   * @param role
   */
  setUserRole(userId: number, role: string): Promise<void> {
    return this.axios.patch(`/user/${userId}`, {
      newRole: role
    })
  }

  /**
   * Lock the account of a user.
   *
   * @param userId
   * @param locked
   */
  setUserLocked(userId: number, locked: boolean): Promise<void> {
    return this.axios.patch(`/user/${userId}`, {
      newLocked: locked
    })
  }
}
