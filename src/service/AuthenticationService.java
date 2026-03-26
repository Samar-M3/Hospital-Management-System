package service;

import service.dto.AuthResult;

/**
 * Abstraction for authenticating a user against any backing store.
 * Implementations can change (DB, API, mock) without touching controllers.
 */
public interface AuthenticationService {
    AuthResult authenticate(String email, String password);
}
