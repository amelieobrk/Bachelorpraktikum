import { AxiosInstance } from 'axios';
import {MajorSection} from "./major";


/**
 * Api access class to access the section endpoints.
 */
export default class MajorSectionsApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Get all sections the user is in regarding to the given major.
   *
   * @param userId
   * @param majorId
   */
  getSectionsByUser(userId: number, majorId: number) : Promise<MajorSection[]> {
    return this.axios.get(`/user/${userId}/major/${majorId}/section`).then(res => {
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
   * Add a major section to the user.
   *
   * @param userId
   * @param majorId
   * @param majorSectionId
   */
  addMajorSectionToUser(userId: number, majorId: number, majorSectionId: number) : Promise<void> {
    return this.axios.put(`/user/${userId}/major/${majorId}/section/${majorSectionId}`).then(() => {})
  }

  /**
   * Remove a major section from the user.
   *
   * @param userId
   * @param majorId
   * @param majorSectionId
   */
  removeMajorSectionFromUser(userId: number, majorId: number, majorSectionId: number) : Promise<void> {
    return this.axios.delete(`/user/${userId}/major/${majorId}/section/${majorSectionId}`).then(() => {})
  }

  /**
   * Get sections assigned to a module
   *
   * @param moduleId
   */
  getSectionsByModule(moduleId: number) : Promise<MajorSection[]> {
    return this.axios.get(`/module/${moduleId}/section`).then(res => res.data)
  }
}
