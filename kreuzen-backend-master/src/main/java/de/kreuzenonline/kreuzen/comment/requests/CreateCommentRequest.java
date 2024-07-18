package de.kreuzenonline.kreuzen.comment.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateCommentRequest {

    @NotNull(message = "CreateCommentRequest-text-not-null")
    @Size(max = 5000, message = "CreateCommentRequest-text-too-long")
    private String text;

}
