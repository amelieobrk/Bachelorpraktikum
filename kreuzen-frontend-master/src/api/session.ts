import { AxiosInstance, CancelToken } from 'axios';
import {mapQuestion, MultipleChoiceQuestion, Question, SingleChoiceQuestion} from "./question";
import {PAGE_SIZE} from "./user";

export interface Session {
  id: number
  name: string
  notes?: string,
  type: string,
  isRandom: boolean,
  isFinished: boolean,
  createdAt: Date,
  updatedAt: Date
}

export interface QuestionResult {
  question: Question
  points: number
  localId: number
}

export interface SingleChoiceSelection {
  localAnswerId: number
  isChecked: boolean
  isCrossed: boolean
}

export interface QuestionStatus {
  time: number
  isSubmitted: boolean
}


export interface Pagination<T> {
  count: number
  entities: T[]
}

export interface MultipleChoiceSelection {
  localAnswerId: number
  isChecked: boolean
  isCrossed: boolean
}

const mapSession = (data: any) : Session => {
  return {
    id: data.id,
    name: data.name,
    notes: data.notes,
    type: data.sessionType,
    isRandom: data.isRandom,
    isFinished: data.isFinished,
    createdAt: new Date(data.createdAt),
    updatedAt: new Date(data.updatedAt)
  }
}

/**
 * Api access class to access the session endpoints.
 */
export default class SessionApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Get amount of available questions with specified filters
   *
   * @param moduleIds
   * @param semesterIds
   * @param tagIds
   * @param questionTypes
   * @param questionOrigins
   * @param textFilter
   */
  getQuestionCount(moduleIds: number[], semesterIds: number[], tagIds: number[], questionTypes: string[], questionOrigins: string[], textFilter: string) : Promise<number> {

    const params = new URLSearchParams();
    if (moduleIds && moduleIds.length > 0) {
      params.append('moduleIds', moduleIds.join(','))
    }
    if (semesterIds && semesterIds.length > 0) {
      params.append('semesterIds', semesterIds.join(','))
    }
    if (tagIds && tagIds.length > 0) {
      params.append('tagIds', tagIds.join(','))
    }
    if (questionTypes && questionTypes.length > 0) {
      params.append('questionTypes', questionTypes.join(','))
    }
    if (questionOrigins && questionOrigins.length > 0) {
      params.append('questionOrigins', questionOrigins.join(','))
    }
    if (textFilter && textFilter !== '') {
      params.append('textFilter', textFilter)
    }

    return this.axios.get(`/session/question/count?${params}`).then(res => res.data.count || 0)
  }

  /**
   * Creates a new session
   *
   * @param name
   * @param moduleIds
   * @param semesterIds
   * @param tagIds
   * @param questionTypes
   * @param questionOrigins
   * @param sessionType
   * @param textFilter
   * @param randomOrder
   */
  createSession(
    name: string,
    moduleIds: number[],
    semesterIds: number[],
    tagIds: number[],
    questionTypes: string[],
    questionOrigins: string[],
    sessionType: string,
    textFilter: string,
    randomOrder: boolean
  ) : Promise<Session> {
    return this.axios.post(`/session`, {
      name,
      sessionType,
      isRandom: randomOrder,
      moduleIds,
      semesterIds,
      tagIds,
      questionTypes,
      questionOrigins,
      filterTerm: textFilter,
    }).then(res => mapSession(res.data))
  }

  /**
   * Gets the session question results.
   *
   * @param id
   */
  getSessionResults(id: number) : Promise<QuestionResult[]> {
    return this.axios.get(`/session/${id}/results`).then(res => res.data);
  }

  /**
   * Gets a session
   *
   * @param id
   */
  getSession(id: number) : Promise<Session> {
    return this.axios.get(`/session/${id}`).then(res => mapSession(res.data))
  }

  /**
   * Deletes a session.
   *
   * @param id
   */
  deleteSession(id: number) : Promise<void> {
    return this.axios.delete(`/session/${id}`)
  }

  /**
   * Resets a session
   *
   * @param id
   */
  resetSession(id: number) : Promise<void> {
    return this.axios.patch(`/session/${id}/reset`)
  }

  /**
   * Gets the status of a question by the local id
   *
   * @param sessionId
   * @param localQuestionId
   */
  getLocalQuestionStatus(sessionId: number, localQuestionId: number) : Promise<QuestionStatus> {
    return this.axios.get(`/session/${sessionId}/question/${localQuestionId}/status`).then(res => res.data)
  }

  /**
   * Marks a session as finished.
   *
   * @param sessionId
   */
  finishSession(sessionId: number) : Promise<void> {
    return this.axios.patch(`/session/${sessionId}/submit`);
  }

  /**
   * Marks a questions as finished.
   *
   * @param sessionId
   * @param localQuestionId
   */
  finishQuestion(sessionId: number, localQuestionId: number) : Promise<void> {
    return this.axios.patch(`/session/${sessionId}/question/${localQuestionId}/submit`);
  }

  /**
   * Updates the time taken for a question
   *
   * @param sessionId
   * @param localQuestionId
   * @param time
   */
  setTime(sessionId: number, localQuestionId: number, time: number) : Promise<void> {
    return this.axios.put(`/session/${sessionId}/question/${localQuestionId}/time`, {time})
  }

  /**
   * Sets a single choice answer
   *
   * @param sessionId
   * @param localQuestionId
   * @param checkedLocalAnswerId
   * @param crossedLocalAnswerIds
   */
  setSingleChoiceAnswer(sessionId: number, localQuestionId: number, checkedLocalAnswerId: number, crossedLocalAnswerIds: number[]) : Promise<void> {
    return this.axios.put(`/session/${sessionId}/question/${localQuestionId}/selection`, {
      type: 'single-choice',
      checkedLocalAnswerId: checkedLocalAnswerId,
      crossedLocalAnswerIds: crossedLocalAnswerIds
    });
  }

  /**
   * Sets a multiple choice answer
   *
   * @param sessionId
   * @param localQuestionId
   * @param checkedLocalAnswerIds
   * @param crossedLocalAnswerIds
   */
  setMultipleChoiceAnswer(sessionId: number, localQuestionId: number, checkedLocalAnswerIds: number[], crossedLocalAnswerIds: number[]) : Promise<void> {
    return this.axios.put(`/session/${sessionId}/question/${localQuestionId}/selection`, {
      type: 'multiple-choice',
      checkedLocalAnswerIds: checkedLocalAnswerIds,
      crossedLocalAnswerIds: crossedLocalAnswerIds
    });
  }

  /**
   * Gets the selection for a single choice question.
   *
   * @param sessionId
   * @param localQuestionId
   */
  getSingleChoiceSelection(sessionId: number, localQuestionId: number) : Promise<SingleChoiceSelection[]> {
    return this.axios.get(`/session/${sessionId}/question/${localQuestionId}/selection`).then(res => res.data)
  }

  /**
   * Gets the selection for a multiple choice question.
   *
   * @param sessionId
   * @param localQuestionId
   */
  getMultipleChoiceSelection(sessionId: number, localQuestionId: number) : Promise<MultipleChoiceSelection[]> {
    return this.axios.get(`/session/${sessionId}/question/${localQuestionId}/selection`).then(res => res.data)
  }

  /**
   * Gets a question by the local id
   *
   * @param sessionId
   * @param localQuestionId
   */
  getLocalQuestion(sessionId: number, localQuestionId: number) : Promise<SingleChoiceQuestion | MultipleChoiceQuestion> {
    return this.axios.get(`/session/${sessionId}/question/${localQuestionId}`).then(res => mapQuestion(res.data))
  }

  /**
   * Gets the amount of questions a session contains.
   *
   * @param sessionId
   */
  getQuestionCountOfSession(sessionId: number) : Promise<number> {
    return this.axios.get(`/session/${sessionId}/question/count`).then(res => res.data.count);
  }

  /**
   * Loads a list of sessions that belong to a user.
   *
   * @param userId
   * @param page
   * @param searchTerm
   * @param cancelToken
   */
  loadSessionsPaginated(userId: number, page: number, searchTerm: string | null = null, cancelToken : CancelToken | undefined = undefined) : Promise<Pagination<Session>> {
    return this.axios.get(`/user/${userId}/session?skip=${page * PAGE_SIZE}&limit=${PAGE_SIZE}`).then(res => {
      const page : Pagination<Session> = {
        count: res.data.count,
        entities: res.data.entities.map(mapSession)
      }
      return page;
    });
  }

}
