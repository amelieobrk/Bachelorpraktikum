package de.kreuzenonline.kreuzen.user;

import java.util.List;

public interface UserService {

    /**
     * Gets a user by his id.
     *
     * @param id Id of the user
     * @return User for given id.
     */
    User getById(int id);

    /**
     * Deletes a user by the given id.
     *
     * @param id id of the user
     */
    void deleteById(int id);

    /**
     * Updates a user with the information included in the overmitted request.
     * Before the user is updated, there are several checks, e.g. whether the new information still matches the requirements.
     * - username or email collisions
     * - does the user information still meet the requirements, e.g. a username should have 3 to 64 characters
     * - all parameters can be null, when they shall not be updated
     *
     * @param newUsername     new Username
     * @param newFirstName    new first name
     * @param newLastName     new last name
     * @param id              userId, gathered by URI
     * @param newUniversityId new university id
     * @param newPassword     new password
     * @return the updated User
     */
    User updateUser(String newUsername,
                    String newEmail,
                    String newFirstName,
                    String newLastName,
                    int id,
                    Integer newUniversityId,
                    String newPassword,
                    String newRole,
                    Boolean newLocked
    );

    /**
     * Returns a list of users using limit and skip ordered by username.
     *
     * @param limit max length of returned list
     * @param skip  amount of users to skip
     * @return list of users
     */
    List<User> getByPagination(int limit, int skip);

    /**
     * Returns a list of users using limit and skip ordered by username. Only users who contain the search
     * term somewhere are listed.
     *
     * @param searchTerm Search term
     * @param limit      max length of returned list
     * @param skip       amount of users to skip
     * @return list of users
     */
    List<User> getByPagination(String searchTerm, int limit, int skip);

    /**
     * Count the amount of registered users.
     *
     * @return amount of users
     */
    long getCount();

    /**
     * Count the amount of registered users who contains search term.
     *
     * @return amount of users
     */
    long getCount(String searchTerm);
}
