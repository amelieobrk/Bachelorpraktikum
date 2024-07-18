import React from 'react';
import { Route, Redirect, Switch } from 'react-router-dom';
import AuthContext from './contexts/AuthContext';

// Layouts
import MainLayout from "./layouts/MainLayout";
import UnauthenticatedLayout from "./layouts/UnauthenticatedLayout";

// Registration
import Login from './pages/Registration/Login';
import Register from './pages/Registration/Register';
import ConfirmEmail from './pages/Registration/ConfirmEmail';
import RequestPasswordReset from './pages/Registration/RequestPasswordReset';
import ConfirmPasswordReset from './pages/Registration/ConfirmPasswordReset';

// User
import Home from './pages/User/Home';
import AddQuestions from "./pages/User/Question/AddQuestions";
import AddQuestionsSpecific from "./pages/User/Question/AddQuestionsSpecific";
import Questions from "./pages/User/Question/Questions";
import Bugs from "./pages/User/Bugs";
import UserSettings from "./pages/User/Settings";
import QuestionSpecific from "./pages/User/Question/QuestionSpecific";
import Help from "./pages/User/Help";
import Guide from "./pages/User/Guide";
import FAQ from "./pages/User/FAQ";

// Session
import CreateSession from "./pages/User/Session/CreateSession";
import SessionsOverview from "./pages/User/Session/SessionsOverview";
import SessionInspect from "./pages/User/Session/SessionInspect";
import SessionLearn from "./pages/User/Session/SessionLearn";
import SessionReview from "./pages/User/Session/SessionReview";

// Admin
import Semester from "./pages/Admin/Semester";
import Module from "./pages/Admin/Module";
import Tags from "./pages/Admin/Tags";
import UserManagementList from "./pages/Admin/UserManagementList";
import UserManagementSpecific from "./pages/Admin/UserManagementSpecific";
import ExamOverview from "./pages/Admin/ExamOverview";

// Misc
import NoPermission from "./pages/Misc/NoPermission";
import NotFound from "./pages/Misc/NotFound";
import Loading from "./pages/Misc/Loading";
import InProgress from "./pages/Misc/InProgress";

const SudoRoutes = () => {
  return (
    <Switch>
      <Route exact path="/admin/semester">
        <Semester />
      </Route>
      <Route exact path="/admin/modules">
        <Module />
      </Route>
      <Route exact path="/admin/tags">
        <Tags />
      </Route>
      <Route exact path="/admin/users">
        <UserManagementList />
      </Route>
      <Route exact path="/admin/exams">
        <ExamOverview />
      </Route>
      <Route exact path="/admin/questions">
        <InProgress />
      </Route>
      <Route exact path="/user/stats">
        <InProgress />
      </Route>
      <Route exact path="/admin/users/:userId">
        {
          (p) =>
            <UserManagementSpecific userId={parseInt(p.match?.params?.userId || 0)} />
        }
      </Route>
      <Route>
        <NotFound />
      </Route>
    </Switch>
  )
}

const AdminRoutes = () => {
  return (
    <Switch>
      <Route exact path="/admin/semester">
        <Semester />
      </Route>
      <Route exact path="/admin/modules">
        <Module />
      </Route>
      <Route exact path="/admin/tags">
        <Tags />
      </Route>
      <Route exact path="/admin/users">
        <UserManagementList />
      </Route>
      <Route exact path="/admin/exams">
        <ExamOverview />
      </Route>
      <Route exact path="/admin/questions">
        <InProgress />
      </Route>
      <Route exact path="/admin/users/:userId">
        {
          (p) =>
            <UserManagementSpecific userId={parseInt(p.match?.params?.userId || 0)} />
        }
      </Route>
      <Route>
        <NotFound />
      </Route>
    </Switch>
  )
}

const ModeratorRoutes = () => {
  return (
    <Switch>
      <Route exact path="/admin/semester">
        <NoPermission />
      </Route>
      <Route exact path="/admin/modules">
        <Module />
      </Route>
      <Route exact path="/admin/exams">
        <ExamOverview />
      </Route>
      <Route exact path="/admin/tags">
        <Tags />
      </Route>
      <Route path="/admin">
        <NoPermission />
      </Route>
      <Route>
        <NotFound />
      </Route>
    </Switch >
  )
}

const UserRoutes = () => {
  return (
    <Switch>
      <Route path="/admin">
        <NoPermission />
      </Route>
      <Route>
        <NotFound />
      </Route>
    </Switch>
  )
}

const RoleSpecificRoutes = (props: { role: string | null | undefined }) => {
  switch (props.role) {
    case "SUDO":
      return <SudoRoutes />
    case "ADMIN":
      return <AdminRoutes />
    case "MOD":
      return <ModeratorRoutes />
    default:
      return <UserRoutes />
  }
}

/**
 * Global Router. All routes should be registered here.
 */
function Routes() {

  return (
    <AuthContext.Consumer>
      {
        (auth) => {
          if (!auth.isInit) {
            // Checking auth token
            return <Loading />;

          } else if (auth.token && auth.user) {

            return (
              <MainLayout>
                <Switch>

                  <Route exact path="/">
                    <Home userId={auth.user.id}/>
                  </Route>

                  <Route exact path="/me/settings">
                    <UserSettings
                      logout={auth.logout}
                      userId={auth.user.id}
                      universityId={auth.user.universityId}
                      username={auth.user.username}
                      email={auth.user.email}
                      firstName={auth.user.firstName}
                      lastName={auth.user.lastName}
                      reloadAuth={auth.reloadAuth}
                    />
                  </Route>

                  <Route exact path="/user/questions">
                    <Questions userId={auth.user.id} />
                  </Route>

                  <Route exact path="/user/questions/add">
                    <AddQuestions userId={auth.user.id} />
                  </Route>
                  <Route path="/user/questions/add/:courseId">
                    {
                      (p) =>
                        <AddQuestionsSpecific courseId={parseInt(p.match?.params?.courseId || 0)} />
                    }
                  </Route>
                  <Route exact path="/user/questions/:questionId">
                    {
                      (p) =>
                        <QuestionSpecific userId={auth.user?.id || 0} questionId={parseInt(p.match?.params?.questionId || 0)} />
                    }
                  </Route>

                  <Route exact path="/user/report-bugs">
                    <Bugs />
                  </Route>

                  <Route exact path="/user/sessions">
                    <SessionsOverview userId={auth.user.id} />
                  </Route>
                  <Route exact path="/user/sessions/new">
                    <CreateSession universityId={auth.user.universityId} />
                  </Route>
                  <Route exact path="/user/sessions/:sessionId">
                    {
                      (p) => <SessionInspect sessionId={parseInt(p.match?.params?.sessionId)} />
                    }
                  </Route>
                  <Route exact path="/user/sessions/:sessionId/learn/:questionId">
                    {
                      (p) => <SessionLearn sessionId={parseInt(p.match?.params?.sessionId)} questionId={parseInt(p.match?.params?.questionId)} />
                    }
                  </Route>
                  <Route exact path="/user/sessions/:sessionId/learn">
                    {
                      (p) => <Redirect to={`/user/sessions/${parseInt(p.match?.params?.sessionId)}/learn/1`} />
                    }
                  </Route>
                  <Route exact path="/user/sessions/:sessionId/review/:questionId">
                    {
                      (p) => <SessionReview sessionId={parseInt(p.match?.params?.sessionId)} questionId={parseInt(p.match?.params?.questionId)} />
                    }
                  </Route>

                  <Route exact path="/help">
                    <Help />
                  </Route>
                  <Route exact path="/help/guide">
                    <Guide />
                  </Route>
                  <Route exact path="/help/faq">
                    <FAQ />
                  </Route>
                  <Route exact path="/user/stats">
                    <InProgress />
                  </Route>

                  <Route>
                    <RoleSpecificRoutes role={auth.user.role} />
                  </Route>

                </Switch>
              </MainLayout>
            );
          } else {
            // Unauthorized
            return (
              <UnauthenticatedLayout>
                <Switch>
                  <Route exact path="/">
                    <Login />
                  </Route>
                  <Route exact path="/register">
                    <Register />
                  </Route>
                  <Route exact path="/confirm-email">
                    <ConfirmEmail />
                  </Route>
                  <Route exact path="/request-password-reset">
                    <RequestPasswordReset />
                  </Route>
                  <Route exact path="/confirm-password-reset">
                    <ConfirmPasswordReset />
                  </Route>
                  <Route path="/">
                    <Redirect to="/" />
                  </Route>
                </Switch>
              </UnauthenticatedLayout>
            )
          }
        }
      }
    </AuthContext.Consumer>
  );
}

export default Routes;
