import { AxiosInstance } from 'axios';

export interface Hint {
  id: number
  isActive: Boolean
  text: String
}

/**
 * Api access class to access the major endpoints.
 */
export default class HintApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * gets a random Hint
   */
  getRandomHint(): Promise<Hint> {
    return this.axios.get(`/hint/random`)
      .then(res => {
        const randomHint: Hint = {
          id: res.data.id,
          isActive: res.data.isActive,
          text: res.data.text
        }
        return randomHint;
      })
  }
}