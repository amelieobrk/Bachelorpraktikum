import React, { ChangeEvent, FormEvent, useEffect, useState } from 'react';
import { Button, Card, Col, Form } from 'react-bootstrap';
import { Module } from "../../../api/modules";
import { Tag } from "../../../api/tags";
import { Semester } from "../../../api/semester";
import api from "../../../api";
import { Link, useHistory } from "react-router-dom";
import { prettyPrintDate } from "../../../utils";
import { AxiosError } from "axios";
import CardHeader from "../../../components/General/CardHeader";

/**
 * Page to create a new session.
 */
export default function CreateSession(props: { universityId: number }) {

  const { universityId } = props;

  const history = useHistory();

  const [questionCount, setQuestionCount] = useState<number>(0);

  const [availableModules, setAvailableModules] = useState<Module[]>([])
  const [availableSemesters, setAvailableSemesters] = useState<Semester[]>([])
  const [availableTags, setAvailableTags] = useState<Tag[]>([])

  const [name, setName] = useState<string>(`Session vom ${prettyPrintDate(new Date())}`);
  const [selectedModules, setSelectedModules] = useState<Module[]>([])
  const [selectedSemesters, setSelectedSemesters] = useState<Semester[]>([])
  const [selectedTags, setSelectedTags] = useState<Tag[]>([]);
  const [questionTypes, setQuestionTypes] = useState<string[]>(['single-choice', 'multiple-choice']);
  const [questionOrigins, setQuestionOrigins] = useState<string[]>(['ORIG']);
  const [sessionType, setSessionType] = useState<string>('practice')
  const [textFilter, setTextFilter] = useState<string>('')
  const [randomOrder, setRandomOrder] = useState<boolean>(true)

  const [isSubmitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [nameError, setNameError] = useState<string | null>(null);

  useEffect(() => {
    api.module.getModuleByUniversity(universityId).then(setAvailableModules);
    api.semester.getSemesters().then(setAvailableSemesters);
  }, [universityId])
  useEffect(() => {
    Promise.all(selectedModules.map(module => api.tag.getTags(module.id))).then(x => x.flat()).then(setAvailableTags)
  }, [selectedModules])
  useEffect(() => {
    api.session.getQuestionCount(
      selectedModules.map(x => x.id),
      selectedSemesters.map(x => x.id),
      selectedTags.map(x => x.id),
      questionTypes,
      questionOrigins,
      textFilter
    ).then(setQuestionCount)
  }, [selectedModules, selectedSemesters, selectedTags, questionTypes, questionOrigins, textFilter])

  const handleToggleQuestionOrigin = (id: string) => {
    if (questionOrigins.includes(id)) {
      setQuestionOrigins(o => o.filter(x => x !== id))
    } else {
      setQuestionOrigins(o => [...o, id])
    }
  }

  const handleToggleQuestionType = (type: string) => {
    if (questionTypes.includes(type)) {
      setQuestionTypes(t => t.filter(x => x !== type))
    } else {
      setQuestionTypes(t => [...t, type])
    }
  }

  const handleChangeModules = (ids: number[]) => {
    const modules: Module[] = [];
    ids.forEach(m => {
      const module: Module | undefined = availableModules.find(x => x.id === m);
      if (module) {
        modules.push(module);
      }
    })
    setSelectedModules(
      modules
    )
  }

  const handleChangeSemesters = (ids: number[]) => {
    const semesters: Semester[] = [];
    ids.forEach(m => {
      const semester: Semester | undefined = availableSemesters.find(x => x.id === m);
      if (semester) {
        semesters.push(semester);
      }
    })
    setSelectedSemesters(
      semesters
    )
  }

  const handleChangeTags = (ids: number[]) => {
    const tags: Tag[] = [];
    ids.forEach(m => {
      const tag: Tag | undefined = availableTags.find(x => x.id === m);
      if (tag) {
        tags.push(tag);
      }
    })
    setSelectedTags(
      tags
    )
  }

  const getMultipleSelected = (e: ChangeEvent<HTMLSelectElement>): number[] => {
    return Array.from(e.target.selectedOptions, option => option.value).map(x => parseInt(x));
  }

  const handleCreateSession = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    // Validate
    setError(null);
    setNameError(null);
    if (questionCount <= 0) {
      setError('Es wurden keine Fragen für deine Session gefunden.');
      return;
    }
    if (!name || name === '' || name.length < 3) {
      setNameError('Bitte gib einen Namen ein, der länger als 3 Zeichen ist.');
      return;
    }
    setSubmitting(true);
    api.session.createSession(
      name,
      selectedModules.map(x => x.id),
      selectedSemesters.map(x => x.id),
      selectedTags.map(x => x.id),
      questionTypes,
      questionOrigins,
      sessionType,
      textFilter,
      randomOrder
    ).then(session => {
      setSubmitting(false);
      history.push(`/user/sessions/${session.id}`);
    }).catch((err: AxiosError) => {
      setSubmitting(false);
      setError(err?.response?.data?.msg || 'Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.');
    })
  }

  return (
    <Card>
      <Card.Body>
        <CardHeader text="Session erstellen" />
        <Form onSubmit={handleCreateSession}>

          <Form.Group controlId="name">
            <Form.Label><h3>Name</h3></Form.Label>
            <Form.Control
              placeholder="Name"
              onChange={(e) => setName(e.target.value)}
              value={name}
              isInvalid={nameError != null}
              disabled={isSubmitting}
            />
            <Form.Control.Feedback type="invalid" >{nameError}</Form.Control.Feedback>
          </Form.Group>

          <Form.Row>
            <Form.Group as={Col} sm={12} md={4} controlId="modules">
              <Form.Label><h3>Module</h3></Form.Label>
              <Form.Control
                as="select"
                multiple
                onChange={(e: ChangeEvent<HTMLSelectElement>) => handleChangeModules(getMultipleSelected(e))}
                value={selectedModules.map(x => String(x.id))}
                disabled={isSubmitting}
              >
                {
                  availableModules.map(module => (
                    <option key={module.id} value={module.id}>{module.name}</option>
                  ))
                }
              </Form.Control>
            </Form.Group>
            <Form.Group as={Col} sm={12} md={4} controlId="semesters">
              <Form.Label><h3>Semester</h3></Form.Label>
              <Form.Control
                as="select"
                multiple
                onChange={(e: ChangeEvent<HTMLSelectElement>) => handleChangeSemesters(getMultipleSelected(e))}
                value={selectedSemesters.map(x => String(x.id))}
                disabled={isSubmitting}
              >
                {
                  availableSemesters.map(semester => (
                    <option key={semester.id} value={semester.id}>{semester.name}</option>
                  ))
                }
              </Form.Control>
            </Form.Group>
            <Form.Group as={Col} sm={12} md={4} controlId="tags">
              <Form.Label><h3>Tags</h3></Form.Label>
              <Form.Control
                as="select"
                multiple
                onChange={(e: ChangeEvent<HTMLSelectElement>) => handleChangeTags(getMultipleSelected(e))}
                value={selectedTags.map(x => String(x.id))}
                disabled={isSubmitting}
              >
                {
                  availableTags.map(tag => (
                    <option key={tag.id} value={tag.id}>{tag.name}</option>
                  ))
                }
              </Form.Control>
            </Form.Group>
          </Form.Row>
          <p className="text-primary">
            (Mehrere können mit Shift oder Alt ausgewählt werden)
          </p>

          <Form.Group controlId="questionType">
            <p style={{ marginBottom: '0.5rem' }}><h3>Fragentyp</h3></p>
            <Form.Check
              id="single-choice"
              inline
              label="Single-Choice"
              type="checkbox"
              onChange={() => handleToggleQuestionType('single-choice')}
              checked={questionTypes.includes('single-choice')}
              disabled={isSubmitting}
            />
            <Form.Check
              id="multiple-choice"
              inline
              label="Multiple-Choice"
              type="checkbox"
              onChange={() => handleToggleQuestionType('multiple-choice')}
              checked={questionTypes.includes('multiple-choice')}
              disabled={isSubmitting}
            />
          </Form.Group>

          <Form.Group controlId="origin">
            <p style={{ marginBottom: '0.5rem' }}><h3>Fragenherkunft</h3></p>
            <Form.Check
              id="ORIG"
              inline
              label="Originalfragen"
              type="checkbox"
              onChange={() => handleToggleQuestionOrigin('ORIG')}
              checked={questionOrigins.includes('ORIG')}
              disabled={isSubmitting}
            />
            <Form.Check
              id="GEPR"
              inline
              label="Gedächtnisprotokoll-Fragen"
              type="checkbox"
              onChange={() => handleToggleQuestionOrigin('GEPR')}
              checked={questionOrigins.includes('GEPR')}
              disabled={isSubmitting}
            />
            <Form.Check
              id="NIKL"
              inline
              label="Nicht-Klausur-Fragen"
              type="checkbox"
              onChange={() => handleToggleQuestionOrigin('NIKL')}
              checked={questionOrigins.includes('NIKL')}
              disabled={isSubmitting}
            />
            <Form.Check
              id="IMPP"
              inline
              label="IMPP-Fragen"
              type="checkbox"
              onChange={() => handleToggleQuestionOrigin('IMPP')}
              checked={questionOrigins.includes('IMPP')}
              disabled={isSubmitting}
            />
          </Form.Group>

          <Form.Group controlId="sessionType">
            <p style={{ marginBottom: '0.5rem' }}><h3>Sessiontyp</h3></p>
            <Form.Check
              id="practice"
              inline
              label="Lernsession"
              type="radio"
              onChange={() => setSessionType('practice')}
              checked={sessionType === 'practice'}
              disabled={isSubmitting}
            />
            <Form.Check
              id="exam"
              inline
              label="Prüfungssession"
              type="radio"
              onChange={() => setSessionType('exam')}
              checked={sessionType === 'exam'}
              disabled={isSubmitting}
            />
          </Form.Group>

          <Form.Group controlId="textFilter">
            <Form.Label><h3>Textfilter</h3></Form.Label>
            <Form.Control
              placeholder="Textfilter (optional)"
              onChange={(e) => setTextFilter(e.target.value)}
              value={textFilter}
              disabled={isSubmitting}
            />
          </Form.Group>

          <Form.Group controlId="random">
            <Form.Check
              id="random"
              label="Zufällige Ordnung"
              type="switch"
              onChange={() => setRandomOrder(x => !x)}
              checked={randomOrder}
              disabled={isSubmitting}
            />
          </Form.Group>

          <p style={{ marginTop: 32 }}>
            <i>Anzahl an Fragen:</i> {questionCount}
          </p>

          {
            error && (
              <p style={{ marginTop: 16 }} className="text-danger">
                {error}
              </p>
            )
          }

          <div>
            <Button
              variant="success"
              type="submit"
              disabled={isSubmitting}
            >
              Session anlegen
            </Button>
            <Button
              variant="secondary"
              className="float-right"
              as={Link}
              to="/user/sessions"
              disabled={isSubmitting}
            >
              Zurück
            </Button>
          </div>
        </Form>
      </Card.Body>
    </Card>
  );
}
