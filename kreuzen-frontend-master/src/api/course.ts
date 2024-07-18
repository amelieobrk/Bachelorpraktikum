import { AxiosInstance } from 'axios';

export interface Course {
  id: number
  moduleId: number
  semesterId: number
  name: string
}

/**
 * Api access class to access the major endpoints.
 */
export default class CourseApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * gets a specific Course
   */
  getCourse(id: number): Promise<Course> {
    return this.axios.get(`/course/${id}`)
      .then(res => {
        const course: Course = {
          id: res.data.id,
          moduleId: res.data.moduleId,
          semesterId: res.data.semesterId,
          name: res.data.name
        }
        return course;
      })
  }

  /**
   * deletes a course 
   */
  deleteCourse(id: number): Promise<void> {
    return this.axios.delete(`/course/${id}`)
      .then(() => { })
  }

  /**
   * Update a Course
   */
  updateCourse(id: number, moduleId: number, semesterId: number): Promise<void> {
    return this.axios.patch(`course/${id}`, {
      id,
      moduleId,
      semesterId
    }).then(() => { })
  }

  /**
   * Gets all courses of a Module
   */
  getCoursesOfModule(moduleId: number): Promise<Course[]> {
    return this.axios.get(`module/${moduleId}/course`)
      .then(res => res.data)
  }

  /**
   * creates a Course
   */
  createCourse(semesterId: number, moduleId: number) {
    return this.axios.post(`/module/${moduleId}/course`, {
      semesterId,
      moduleId
    })
      .then(() => { })
  }

}