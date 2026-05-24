Payment Demo Project
A hands-on Spring Boot payment integration demo built step by step to move beyond basic CRUD and understand how real payment systems behave.

Overview
This project started as a simple Version 1 backend with two core capabilities: creating an order and creating a payment linked to that order. The next iterations extend that base into a more realistic payment flow with idempotency, payment lifecycle states, webhook-style updates, and a lightweight frontend for testing the flow end to end.

The goal is to learn how payment integrations differ from normal CRUD APIs. Instead of treating every POST request as “create a new row,” the application is designed to handle retries safely, track a payment through multiple states, and update local system state based on asynchronous events.

Why this project exists
Many beginner backend projects stop at CRUD operations. Real product companies, especially payments companies, care about stronger backend instincts: reliability, duplicate prevention, state transitions, and event-driven updates.

This project is designed to practice exactly those ideas in a small and understandable codebase.

What this project teaches
Building REST APIs with Spring Boot.

Creating an order and linking payments to that order.

Using idempotency keys to prevent duplicate payment creation on retries.
​

Understanding why one payment object should represent one order or customer session.
​

Modeling payment lifecycle states such as processing, success, and failure based on the broader PaymentIntent lifecycle.

Simulating webhook-style event handling to update backend state asynchronously.
​

Connecting a frontend UI to a backend and testing integration behavior in full context.

Core concepts
1. Idempotency
Stripe supports idempotency on all POST requests so a client can safely retry a request without accidentally creating duplicate objects or duplicate side effects. Stripe stores the first status code and response body for a given idempotency key and returns the same result for subsequent retries with that same key.
​

In this project, that idea is simulated by saving a payment against an idempotencyKey and returning the existing payment if the same key is used again.
​

2. Payment lifecycle
A PaymentIntent is designed to guide the full process of collecting a payment and can move through multiple statuses during its lifetime. Stripe recommends creating exactly one PaymentIntent for each order or customer session.
​

This demo simplifies that idea into statuses like PROCESSING, SUCCESS, and FAILED, which makes it easier to understand asynchronous payment behavior before integrating a real payment provider.

3. Webhook-style updates
Stripe provides webhook endpoints so systems can be notified when events happen in an account. That model is important because many payment outcomes happen asynchronously rather than in the original request-response cycle.
​

This project simulates a webhook endpoint so payment and order state can be updated later, which is a more realistic backend pattern than immediately marking every payment successful.

Current project flow
Create an order.

Create a payment for that order.

Save and reuse the idempotency key to prevent duplicate payment creation.
​

Start the payment in a pending or processing state.

Simulate a webhook event to mark the payment as successful or failed.

Update the linked order status based on the payment result.

Tech stack
Layer	Technology
Backend	Java, Spring Boot
API style	REST
Database	H2 / JPA
ORM	Spring Data JPA
Frontend	Streamlit
Testing	Postman
Why this is valuable for companies
This project helps build the kind of backend thinking companies value in real engineering work:

Reliability under retries, because real networks fail and clients retry requests.
​

Safer payment creation, because duplicate requests should not create duplicate charges or duplicate payment objects.

Better state modeling, because real payment systems are not a one-step CRUD insert.

Event-driven thinking, because payment systems often update application state through later notifications and webhooks.
​

Stronger API integration skills, because the backend must coordinate its own data with an external-style workflow.

These patterns are much closer to production backend engineering than simple CRUD applications.

Planned improvements
Add DTOs and cleaner response handling.

Add order and payment lookup endpoints.

Improve repository methods for payment lookup by reference.

Extend Streamlit UI to simulate success and failure events.

Add validation and better error handling.

Replace the fake payment flow with a real Stripe test-mode integration.

Running the project
Backend
Open the Spring Boot project.

Run the application.

Make sure the backend starts on http://localhost:8080.

Frontend
Open the Streamlit UI folder.

Install dependencies:

bash
pip install -r requirements.txt
Run the Streamlit app:

bash
streamlit run app.py
Open the local Streamlit URL, usually http://localhost:8501.

Example API calls
Create order
text
POST /api/orders?productName=Book&amount=500
Create payment
text
POST /api/payments?orderId=1&idempotencyKey=abc123
Simulate webhook
text
POST /api/webhook
Content-Type: application/json

{
  "paymentReference": "PAY_12345",
  "eventType": "payment_intent.succeeded"
}
Learning outcome
The main outcome of this project is the mindset shift from CRUD development to integration-focused backend engineering. The codebase is intentionally small so the important ideas are easier to understand: safe retries, state transitions, asynchronous updates, and reliable backend behavior under real-world conditions.
