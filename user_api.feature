Feature: Public API validation
  As an API tester
  I want to validate sample GET and POST endpoints
  So that I can verify the framework works

  Scenario: Retrieve a single todo item from JSONPlaceholder
    Given the API base URL "https://jsonplaceholder.typicode.com"
    When I send a GET request to "/todos/1"
    Then the response status code should be 200
    And the response should contain "delectus aut autem"

  Scenario: Create a new todo item using JSONPlaceholder
    Given the API base URL "https://jsonplaceholder.typicode.com"
    When I send a POST request to "/todos" using file "payloads/todo_payload.json"
    Then the response status code should be 201
    And the response should contain "Test API"
