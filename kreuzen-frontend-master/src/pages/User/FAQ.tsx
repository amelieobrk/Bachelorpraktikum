import React from 'react';
import { Card, Table } from 'react-bootstrap';

/*
* Displays frequently used Questions
*
*/
export default function FAQ() {
  return (
    <>
      <Card className="bg-primary">
        <Card.Body>
          <Card.Title className="text-white">
            FAQ
          </Card.Title>
          <Table className="bg-white" striped bordered hover>
            <thead>
              <tr>
                <th>
                  Frage
                </th>
                <th>
                  Antwort
                </th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>
                  Wer seid ihr?
                </td>
                <td>
                  Wir sind das DEFI Team! Wir sind eine Gruppe von Medizinstudenten/innen an der Uni-Frankfurt, die das Studium für uns alle verbessern wollen.
                  Mehr über uns findest du hier. Wir freuen uns immer über neue Teammitglieder!
                </td>
              </tr>
              <tr>
                <td>
                  Habt ihr keine Angst, dass die Fähigkeiten der Studierenden durch das Kreuzen abnehmen?
                </td>
                <td>
                  Nein! Wir sind der Meinung, dass das Kreuzen lediglich dazu dient uns die wichtigsten Fakten hervorzuheben. Eine gut durchdachte Klausure prüft, unserer Meinung nach, medizinisch relevante Dinge ab. DEFI-KO ist daher ein Werkzeug, das es uns ermöglicht uns gezielter auf die wichtigen Dinge vorzubereiten und diese zu lernen. Uns geht es nicht darum das "Auswendiglernen" zu unterstützen, sondern vielmehr darum einen roten Faden durch die überwältigend Großen Stoffgebiete zu ziehen.
                </td>
              </tr>
              <tr>
                <td>
                  Sind die Klausurfragen nicht urheberrechtlich geschützt?
                </td>
                <td>
                  DEFI-KO erlaubt ausdrücklich keine Originalfragen! In unserer Datenbank befinden sich lediglich Fragen aus dem vorklinischen Abschnitt. Hier erhalten die Studierenden die Fragen in Papierform nach der Klausur. Die Rechte an den Fragen liegen explizit bei den jeweiligen Instituten. Viele von uns sind im PJ oder haben ihr Studium abgeschlossen und können daher nicht beurteilen ob es sich bei einer von euch eingestellten Frage um eine Originalfrage handelt. Deswegen kann jeder von euch beim Kreuzen Fragen melden. Außerdem haben wir Moderatoren, die Fragen zuerst freigeben müssen, bevor diese in die Datenbank aufgenommen werden.
                </td>
              </tr>
              <tr>
                <td>
                  Wie gewährleistet ihr die Qualität eurer Fragen?
                </td>
                <td>
                  Unsere Fragen sind eure Fragen! Die Datenbank enthält nur Fragen, die von euch eingegeben wurden. Ihr solltet daher selber darauf achten, dass ihr korrekt formulierte Fragen mit der korrekten Antwortmöglichkeit eingebt. Weiterhin haben wir Moderatoren, die Fragen prüfen und freigeben müssen, bevor sie in die Datenbank aufgenommen werden. Letztendlich kann auch jeder von euch eine Frage melden oder kommentieren während ihr sie kreuzt.
                </td>
              </tr>
            </tbody>
          </Table>
        </Card.Body>
      </Card>
    </>
  );
}