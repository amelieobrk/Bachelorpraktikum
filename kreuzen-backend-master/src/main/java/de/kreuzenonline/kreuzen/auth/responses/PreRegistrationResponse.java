package de.kreuzenonline.kreuzen.auth.responses;

import de.kreuzenonline.kreuzen.university.responses.UniversityResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreRegistrationResponse {

    private List<UniversityResponse> universities;
}
