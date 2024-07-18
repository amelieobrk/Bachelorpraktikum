import { AxiosInstance } from 'axios';

export interface Exam {
  id: number
  courseId: number
  isComplete: boolean
  isRetry: boolean
  name: string
  date: Date
}

const mapExam = (data : {
  id: number
  courseId: number
  isComplete: boolean
  isRetry: boolean
  name: string
  date: string
}) : Exam => {

  const {id, courseId, isComplete, isRetry, name, date} = data;

  return {
    id,
    courseId,
    isComplete,
    isRetry,
    name,
    date: new Date(date),
  }
}

/**
 * Api access class to access the exam endpoints.
 */
export default class ExamApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Get an exam by id
   *
   * @param examId
   */
  getExam(examId: number) : Promise<Exam> {
    return this.axios.get(`/exam/${examId}`).then(res => mapExam(res.data));
  }

  /**
   * Create a new exam
   *
   * @param courseId
   * @param date
   * @param name
   * @param isRetry
   */
  createExam(courseId: number, date: string, name: string, isRetry: boolean) : Promise<void> {
    return this.axios.post(`/course/${courseId}/exam`, {
      name,
      date,
      isRetry
    })
  }

  /**
   * Delete an exam
   *
   * @param examId
   * @param password
   */
  deleteExam(examId: number, password: string) : Promise<void> {
    return this.axios.delete(`/exam/${examId}`, {data: {password}}).then(() => {})
  }

  /**
   * Update an exam
   *
   * @param examId
   * @param date
   * @param name
   * @param isRetry
   * @param isComplete
   */
  updateExam(examId: number, date: string | null, name: string | null, isRetry: boolean | null, isComplete: boolean | null) : Promise<void> {
    return this.axios.patch(`/exam/${examId}`, {
      id: examId,
      date,
      name,
      isRetry,
      isComplete
    }).then(() => {})
  }

  /**
   * Get all exams by a university.
   *
   * @param universityId
   */
  getByUniversity(universityId: number) : Promise<Exam[]> {
    return this.axios.get(`/university/${universityId}/exam`).then(d => d.data.map(mapExam))
  }

  /**
   * Get all exams by a major
   *
   * @param universityId
   * @param majorId
   */
  getByMajor(universityId: number, majorId: number) : Promise<Exam[]> {
    return this.axios.get(`/university/${universityId}/exam?major=${majorId}`).then(d => d.data.map(mapExam))
  }

  /**
   * Get all exams by a semester
   *
   * @param universityId
   * @param semesterId
   */
  getBySemester(universityId: number, semesterId: number) : Promise<Exam[]> {
    return this.axios.get(`/university/${universityId}/exam?semester=${semesterId}`).then(d => d.data.map(mapExam))
  }

  /**
   * Get all exams by a major for a given semester
   *
   * @param universityId
   * @param semesterId
   * @param majorId
   */
  getBySemesterAndMajor(universityId: number, semesterId: number, majorId: number) : Promise<Exam[]> {
    return this.axios.get(`/university/${universityId}/exam?semester=${semesterId}&major=${majorId}`).then(d => d.data.map(mapExam))
  }

  /**
   * Get all exams by a course
   *
   * @param courseId
   */
  getByCourse(courseId: number) : Promise<Exam[]> {
    return this.axios.get(`/course/${courseId}/exam`).then(d => d.data.map(mapExam))
  }
}