import { AxiosInstance } from 'axios';
import {Role} from "./user";

/**
 * Access class to access the role endpoints.
 */
export default class RoleApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Gets a list of all available roles
   */
  getAllRoles() : Promise<Role[]> {
    return this.axios.get(`/role`).then(res => res.data)
  }

}
