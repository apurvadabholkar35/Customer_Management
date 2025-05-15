### Prerequisites

* Java Development Kit (JDK) 17 or higher
* Maven

### Getting Started

1.  **Clone the repository:**

    Open your terminal or command prompt and clone the project repository using Git:

    ```bash
    git clone https://github.com/apurvadabholkar35/Customer_Management.git
    cd customer-management
    ```

2.  **Build the project:**

    Navigate to the project's root directory (`customer-management`) in your terminal and build the project using Maven. This will download dependencies and compile the code:

    ```bash
    mvn clean install
    ```

3.  **Run the application:**

    Execute the Spring Boot application from the project's root directory using the Maven plugin:

    ```bash
    mvn spring-boot:run
    ```

    The application will start, and you should see output indicating that the embedded Tomcat server is initialized and running, typically on port 8080 (as configured in `application.properties`).

### Accessing the API and Documentation

* **API Base URL:** `http://localhost:8080`
* **OpenAPI (Swagger UI):** Access the interactive API documentation at `http://localhost:8080/swagger-ui.html`
* **Raw OpenAPI Spec (YAML):** `http://localhost:8080/v3/api-docs.yaml`
* **Raw OpenAPI Spec (JSON):** `http://localhost:8080/v3/api-docs`
* **H2 Console:** Access the H2 database console at `http://localhost:8080/h2-console`. Use the JDBC URL `jdbc:h2:mem:customerdb`, Username `sa`, and leave the Password blank.

### Running Tests

To execute the unit and integration tests for the project, run the following Maven command in the project's root directory:

```bash
mvn test
