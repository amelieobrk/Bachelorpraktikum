package de.kreuzenonline.kreuzen.email;

public interface EmailService {




    /**
     * Send confirmation email to user for registration process
     *
     * @param to        user email
     * @param token     confirmation token for user
     * @param firstName name of user for message
     */
    void sendConfirmEmailMessage(String to, String token, String firstName);


    /**
     * Send email to user for requested password reset
     *
     * @param to        user email
     * @param token     confirmation token for user
     * @param firstName name of user for message
     */
    void sendPasswordResetMessage(String to, String token, String firstName);

    /**
     * Send email to user when error report is resolved
     *
     * @param to        user email
     * @param firstName name of user for message
     */
    void sendErrorResolvedMessage(String to, String firstName);

    /**
     * Send email to user when error report is deleted without being resolved
     *
     * @param to        user email
     * @param firstName name of user for message
     */
    void sendErrorDeclinedMessage(String to, String firstName);
}
