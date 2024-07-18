import { AxiosInstance } from 'axios';

export interface Semester {
  endYear: number
  id: number
  name: string
  startYear: number
}

/**
 * Api access class to access the semester endpoints.
 */
export default class SemesterApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Gets all semesters.
   * 
   */
  getSemesters(): Promise<Semester[]> {
    return this.axios.get(`/semester`)
      .then(res => {
        const semesters: Semester[] = res.data.map((s: Semester) => {
          const semester: Semester = {
            endYear: s.endYear,
            id: s.id,
            name: s.name,
            startYear: s.startYear
          };
          return semester;
        });
        return semesters;
      })
  }

  /**
   * Delete a Semester
   *
   * @param id
   * @param password
   */
  deleteSemester(id: number, password: string): Promise<void> {
    return this.axios.delete(`/semester/${id}`, { data: { password } })
      .then(() => { })
  }

  /**
   * Create a semester
   *
   * @param name
   * @param startYear
   * @param endYear
   */
  createSemester(name: string, startYear: number, endYear: number): Promise<void> {
    return this.axios.post('/semester', {
      endYear,
      name,
      startYear
    }).then(() => { })
  }

  /**
   * Gets a specific Semester by the Semester id
   *
   * @param id
   */
  getSemesterById(id: number): Promise<Semester> {
    return this.axios.get(`/semester/${id}`, { params: id })
      .then(res => {
        const semester: Semester = {
          endYear: res.data.endYear,
          id: res.data.id,
          name: res.data.name,
          startYear: res.data.startYear
        }
        return semester;
      })
  }

  /**
   * Updates a specific Semester
   *
   * @param id
   * @param endYear
   * @param name
   * @param startYear
   */
  updateSemester(id: number, endYear: number, name: string, startYear: number): Promise<void> {
    return this.axios.patch(`/semester/${id}`, {
      endYear,
      name,
      startYear
    }).then(() => { })
  }
}
