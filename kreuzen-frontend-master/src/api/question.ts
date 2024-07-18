import {AxiosInstance, CancelToken} from 'axios';
import {PAGE_SIZE, Pagination} from "./user";

export interface Question {
  id: number
  text: string
  type: string
  courseId: number
  examId?: number | null | undefined
  points: number
}

export interface SingleChoiceAnswer {
  id: number
  localId: number
  text: string
}

export interface SingleChoiceQuestion {
  type: string
  id: number
  text: string
  additionalInformation?: string
  points: number
  answers: SingleChoiceAnswer[]
  correctAnswerLocalId: number
  courseId: number
  examId?: number | null | undefined
}

export interface MultipleChoiceAnswer {
  id: number
  localId: number
  text: string
  isCorrect: boolean
}

export interface MultipleChoiceQuestion {
  type: string
  additionalInformation?: string
  points: number
  id: number
  text: string
  answers: MultipleChoiceAnswer[]
  courseId: number
  examId?: number | null | undefined
}

export interface Comment {
  id: number
  text: string
  creatorId: number
  createdAt: Date
  updatedAt: Date
}

export const mapQuestion = (data: any) : SingleChoiceQuestion | MultipleChoiceQuestion => {
  if (data.type === 'multiple-choice') {
    return {
      id: data.id,
      text: data.text,
      additionalInformation: data.additionalInformation,
      courseId: data.courseId,
      examId: data.examId,
      points: data.points,
      type: data.type,
      answers: data.answers.map((a: any) => ({
        id: a.id,
        localId: a.localId,
        text: a.text,
        isCorrect: data.correctAnswerLocalIds.includes(a.localId)
      }))
    };
  }
  return data
}

const mapComment = (d: any) => {
  return {
    id: d.id,
    text: d.comment,
    creatorId: d.creatorId,
    createdAt: new Date(d.createdAt),
    updatedAt: new Date(d.updatedAt)
  }
}

/**
 * Access class to access the question endpoints.
 */
export default class QuestionApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Creates a new single choice question
   *
   * @param courseId
   * @param examId
   * @param text
   * @param additionalInformation
   * @param origin
   * @param points
   * @param answers
   * @param correctAnswer
   */
  createSingleChoiceQuestion(courseId: number, examId: number | null, text: string, additionalInformation: string, origin: string, points: number, answers: string[], correctAnswer: number) : Promise<void> {
    return this.axios.post('/question', {
      type: 'single-choice',
      text,
      additionalInformation,
      points,
      courseId,
      examId,
      origin,
      answers,
      correctAnswerLocalId: correctAnswer
    })
  }

  /**
   * Creates a new multiple choice question
   *
   * @param courseId
   * @param examId
   * @param text
   * @param additionalInformation
   * @param origin
   * @param points
   * @param answers
   */
  createMultipleChoiceQuestion(courseId: number, examId: number | null, text: string, additionalInformation: string, origin: string, points: number, answers: { text: string, isCorrect: boolean }[]) : Promise<void> {
    return this.axios.post('/question', {
      type: 'multiple-choice',
      text,
      additionalInformation,
      points,
      courseId,
      examId,
      origin,
      answers: answers.map(a => a.text),
      correctAnswerLocalIds: answers.map((a, i) => a.isCorrect ? i + 1 : -1).filter(x => x !== -1)
    })
  }

  /**
   * Get questions based on search criteria. Pagination is used as many questions may fit the criteria.
   *
   * @param page
   * @param moduleId
   * @param tagId
   * @param courseId
   * @param examId
   * @param searchString
   * @param cancelToken
   */
  getQuestions(page: number, moduleId?: number | undefined, tagId?: number | undefined, courseId?: number | undefined, examId?: number | undefined, searchString?: string | undefined, cancelToken?: CancelToken | undefined) : Promise<Pagination<Question>> {

    const params = new URLSearchParams();
    params.append('skip', String(page * PAGE_SIZE))
    params.append('limit', String(PAGE_SIZE))
    if (moduleId) {
      params.append('moduleId', String(moduleId))
    }
    if (tagId) {
      params.append('tagId', String(tagId))
    }
    if (courseId) {
      params.append('courseId', String(courseId))
    }
    if (examId) {
      params.append('examId', String(examId))
    }
    if (searchString) {
      params.append('searchTerm', searchString)
    }

    return this.axios.get(
      `/question?${params.toString()}`,
      { cancelToken }
    ).then(res => res.data)
  }

  /**
   * Get a question by id
   */
  getQuestion(questionId: number) : Promise<SingleChoiceQuestion | MultipleChoiceQuestion> {
    return this.axios.get(`/question/${questionId}`).then(res => {
      if(res.data.type === 'multiple-choice') {
        const mc : MultipleChoiceQuestion = {
          id: res.data.id,
          text: res.data.text,
          additionalInformation: res.data.additionalInformation,
          courseId: res.data.courseId,
          examId: res.data.examId,
          points: res.data.points,
          type: res.data.type,
          answers: res.data.answers.map((a: any) => ({
            id: a.id,
            localId: a.localId,
            text: a.text,
            isCorrect: res.data.correctAnswerLocalIds.includes(a.localId)
          }))
        }
        return mc;
      }
      return res.data
    })
  }

  /**
   * Get the comments for a question.
   *
   * @param questionId
   */
  getComments(questionId: number) : Promise<Comment[]> {
    return this.axios.get(`/question/${questionId}/comment`).then(res => res.data.map(mapComment));
  }

  /**
   * Create a new comment for a question.
   *
   * @param questionId
   * @param text
   */
  createComment(questionId: number, text: string) : Promise<void> {
    return this.axios.post(`/question/${questionId}/comment`, {text});
  }

  /**
   * Update a comment.
   *
   * @param commentId
   * @param text
   */
  updateComment(commentId: number, text: string) : Promise<void> {
    return this.axios.patch(`/comment/${commentId}`, {text});
  }

  /**
   * Delete a comment.
   *
   * @param commentId
   */
  deleteComment(commentId: number) : Promise<void> {
    return this.axios.delete(`/comment/${commentId}`);
  }
}
