package de.kreuzenonline.kreuzen.auth.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("email_confirmation_token")
public class EmailConfirmationToken {

    @Id
    private Integer userId;
    private String tokenHash;
}
