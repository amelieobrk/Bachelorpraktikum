import { AxiosInstance } from 'axios';
import {University} from "./auth";
import {MajorSection} from "./major";

export interface Major {
  id: number
  name: string
  sections: MajorSection[] | null
}

/**
 * Access class to access the university endpoints.
 */
export default class UniversityApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Get a list of all universities
   */
  getUniversities() : Promise<University[]> {
    return this.axios.get('/university').then(res => {
      const universities : University[] = res.data;
      return universities;
    })
  }

  /**
   * Gets all majors that a university offers
   *
   * @param university
   */
  getMajorsByUniversity(university:University) : Promise<Major[]> {
    return this.axios.get(`/university/${university.id}/major`)
      .then(res => {
        const majors: Major[] = res.data.map((m : Major) => {
          const major : Major = {
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
   * Gets all majors that a university offers
   *
   * @param universityId
   */
  getMajorsByUniversityId(universityId:number) : Promise<Major[]> {
    return this.axios.get(`/university/${universityId}/major`)
      .then(res => {
        const majors: Major[] = res.data.map((m : Major) => {
          const major : Major = {
            id: m.id,
            name: m.name,
            sections: null
          };
          return major;
        });
        return majors;
      })
  }

}
