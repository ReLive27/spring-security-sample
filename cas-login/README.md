## Relevant Information:
This project is a CAS client example implemented using Spring Security, demonstrating how to integrate Single Sign-On (SSO) with a CAS server.

- **Frontend Project**: [cas-vue-template](https://github.com/ReLive27/cas-vue-template)
- **CAS Server Project**: [cas-server-template](https://github.com/ReLive27/cas-server-template)

## Access Example

1. Visit `http://localhost:9528/`, the frontend will redirect the user to the CAS server login.
2. Log in on the CAS server with a test account.
3. After successful login, the user will be redirected back to the frontend and the authentication will be complete.

## Configuration

### Spring Security CAS Client Configuration

The Spring Security CAS client configuration is located in `application.yml`:

```yaml
spring:
  security:
    cas:
      client:
        cas-server-url: http://localhost:8443/cas
        cas-server-login-url: http://localhost:8443/cas/login
        service: http://localhost:9528/login/callback
```

You can modify `cas-server-url`, `cas-server-login-url`, and `service` according to your setup.

