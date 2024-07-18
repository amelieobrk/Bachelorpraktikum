import { AxiosInstance } from 'axios';
import {Major} from "./university";

export interface MajorSection {
  id: number
  name: string
  majorId: number
}

/**
 * Api access class to access the major endpoints.
 */
export default class MajorApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Gets all sections that belong to the given major.
   *
   * @param majorId
   */
  getSectionsByMajor(majorId:number) : Promise<MajorSection[]> {
    return this.axios.get(`/major/${majorId}/section`)
      .then(res => {
        const sections: MajorSection[] = res.data.map((s: MajorSection) => {
          const section : MajorSection = {
            id: s.id,
            name: s.name,
            majorId: s.majorId
          };
          return section;
        });
        return sections;
      })
  }

  /**
   * Add a major to the user.
   *
   * @param userId
   * @param majorId
   */
  addMajorToUser(userId: number, majorId: number) : Promise<void> {
    return this.axios.put(`/user/${userId}/major/${majorId}`).then(() => {})
  }

  /**
   * Remove a major to the user.
   *
   * @param userId
   * @param majorId
   */
  removeMajorFromUser(userId: number, majorId: number) : Promise<void> {
    return this.axios.delete(`/user/${userId}/major/${majorId}`).then(() => {})
  }

  /**
   * Get majors assigned to a module
   *
   * @param moduleId
   */
  getMajorsByModule(moduleId: number) : Promise<Major[]> {
    return this.axios.get(`/module/${moduleId}/major`).then(res => res.data)
  }

}
