package de.kreuzenonline.kreuzen.error.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateErrorRequest {


    @Size(min = 3, message = "CreateErrorReportRequest-comment-too-short")
    @Size(max = 5000, message = "CreateErrorReportRequest-comment-too-long")
    private String comment;

    @Size(max = 250, message = "CreateErrorReportRequest-source-too-long")
    private String source;

    private Boolean isResolved;
}
