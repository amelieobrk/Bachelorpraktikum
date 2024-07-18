import axios, { AxiosInstance } from 'axios';
import AuthApi from './auth';
import UniversityApi from "./university";
import MajorApi from "./major";
import SemesterAPI from "./semester";
import ModuleApi from "./modules";
import CourseApi from "./course";
import TagApi from "./tags";
import UserApi from "./user";
import MajorSectionsApi from "./section";
import RoleApi from "./role";
import ExamApi from "./exam";
import QuestionApi from "./question";
import HintApi from './hint';
import SessionApi from "./session";

/**
 * Wrapper for the api. This contains all api access classes.
 */
interface Api {
  setAuthorization: (token: string) => void
  removeAuthorization: () => void
  axiosInstance: AxiosInstance
  auth: AuthApi
  university: UniversityApi
  major: MajorApi
  semester: SemesterAPI
  module: ModuleApi
  course: CourseApi
  tag: TagApi
  user: UserApi
  section: MajorSectionsApi
  role: RoleApi
  exam: ExamApi
  question: QuestionApi
  hint: HintApi
  session: SessionApi
}

const axiosInstance: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_BACKEND_URL,
});

/**
 * Instance of the api access class
 */
const api: Api = {
  axiosInstance: axiosInstance,
  setAuthorization: ((token: string) => { axiosInstance.defaults.headers.authorization = token; }),
  removeAuthorization: (() => { delete axiosInstance.defaults.headers.authorization; }),
  auth: new AuthApi(axiosInstance),
  university: new UniversityApi(axiosInstance),
  major: new MajorApi(axiosInstance),
  semester: new SemesterAPI(axiosInstance),
  module: new ModuleApi(axiosInstance),
  course: new CourseApi(axiosInstance),
  tag: new TagApi(axiosInstance),
  user: new UserApi(axiosInstance),
  section: new MajorSectionsApi(axiosInstance),
  role: new RoleApi(axiosInstance),
  exam: new ExamApi(axiosInstance),
  question: new QuestionApi(axiosInstance),
  hint: new HintApi(axiosInstance),
  session: new SessionApi(axiosInstance)
};

export default api;