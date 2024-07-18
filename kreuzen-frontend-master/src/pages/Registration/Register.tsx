import React, {useState} from 'react';
import api from "../../api";
import {University} from "../../api/auth";
import {Major} from "../../api/university";
import EnterBasicInformation from "../../components/Registration/EnterAccountInformation";
import RegistrationConfirmation from "../../components/Registration/RegistrationConfirmation";
import RegistrationOverview from "../../components/Registration/RegistrationOverview";
import SelectMajorSection from "../../components/Registration/SelectMajorSection";
import SelectMajor from "../../components/Registration/SelectMajor";
import SelectUniversity from "../../components/Registration/SelectUniversity";

/**
 * Registration Page. It walks the user through a multi-step form.
 * 1. Collect general information like name, email, username
 * 2. Choose university (optional)
 * 3. Choose major
 * 4. Choose major section
 * 5. Overview, confirm legal stuff and register
 * 6. Confirmation
 */
export default function Register() {
  // Input Data
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [university, setUniversity] = useState<University | null>(null);
  const [major, setMajor] = useState<Major[]>([]);

  // Given Data
  const [availableUniversities, setAvailableUniversities] = useState<University[]>([]);

  const [step, setStep] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);

  const onBack = () => {
    setStep(i => i-1);
  }

  const onNextStep1 = (firstName: string, lastName: string, username: string, email: string, password: string) => {
    setError(null);
    setFirstName(firstName);
    setLastName(lastName);
    setUsername(username);
    setEmail(email);
    setPassword(password);
    api.auth.preRegister(username, email).then(t => {
      setAvailableUniversities(t.universities);
      if (university?.id && t.universities.find(u => u.id === university?.id) == null) {
        // This may happen when the user changes his email domain.
        setUniversity(null);
      }
      if (t.universities.length === 0) {
        setError('Bitte nutze Deine studentische Email-Adresse');
      } else if (t.universities.length === 1) {
        setUniversity(t.universities[0]);
        setStep(2);
      } else {
        setStep(1);
      }
    }).catch(e => {
      if (e?.response?.data?.msg) {
        setError(e.response.data.msg);
      } else {
        setError("Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.");
      }
    })
  }

  const onNextStep2 = () => {
    setError(null);
    if (university != null) {
      setStep(2);
    } else {
      setError("Bitte wähle Deine Universität aus.")
    }
  }

  const onNextStep3 = () => {
    setError(null);
    setStep(3);
  }

  const onNextStep4 = () => {
    setError(null);
    setStep(4);
  }

  const onRegister = () => {
    setSubmitting(true);
    setError(null);
    if (university != null) {
      api.auth.register(
        firstName,
        lastName,
        username,
        email,
        password,
        university.id,
        major.map(m => m.id),
        major.map(m => m.sections || []).flat(1).map(s => s.id)
      ).then(() => {
        setSubmitting(false);
        setStep(5);
      }).catch(e => {
        setSubmitting(false);
        if (e?.response?.data?.msg) {
          setError(e.response.data.msg);
        } else {
          setError("Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.");
        }
      })
    } else {
      setError("Es ist keine Universität ausgewählt");
    }
  }

  switch (step) {
    case 0:
      return (
        <EnterBasicInformation
          firstName={firstName}
          lastName={lastName}
          username={username}
          email={email}
          password={password}
          onNext={onNextStep1}
          hidden={step !== 0}
          error={error}
        />
      );
    case 1:
      return (
        <SelectUniversity
          availableUniversities={availableUniversities}
          university={university}
          setUniversity={setUniversity}
          onNext={onNextStep2}
          onBack={onBack}
        />
      );
    case 2:
      return (
        <SelectMajor
          majors={major}
          setMajors={setMajor}
          error={error}
          onNext={onNextStep3}
          onBack={onBack}
          university={university}
        />
      );
    case 3:
      return (
        <SelectMajorSection
          majors={major}
          setMajors={setMajor}
          onNext={onNextStep4}
          onBack={onBack}
        />
      );
    case 4:
      return (
        <RegistrationOverview
          firstName={firstName}
          lastName={lastName}
          username={username}
          email={email}
          university={university}
          major={major}
          error={error}
          register={onRegister}
          onBack={onBack}
          submitting={submitting}
        />
      );
    case 5:
      return (
        <RegistrationConfirmation
          firstName={firstName}
          email={email}
        />
      );
    default:
      return null;
  }
}
