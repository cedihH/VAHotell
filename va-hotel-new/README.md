# VA Hotel — new local project

This is a separate Spring Boot + Java 21 + Maven + SQLite project. The uploaded Matcha Lover Hotel ZIP was used as a reference for its MVC structure, responsive styling, registration/login flow, CAPTCHA pattern, manager workspace, reservation workflow, room inventory and chatbot integration. The source project itself is not included as the VA Hotel application.

## Run
Install JDK 21 and Maven 3.9+. In the project root run:
```bash
mvn clean spring-boot:run
```
Open `http://localhost:8080`. The SQLite database is created under `./data/va-hotel.db`.

Development accounts (all use `ChangeMe123!`): `admin@localhost` (Super Admin), `online@localhost` (Online Manager), and `onsite@localhost` (On-Site Manager). Change these before sharing the project. The starter creates them automatically.

## Included starter workflows
- Public home and room browsing pages, with manager-maintained room inventory.
- Database-backed login with BCrypt and separate Customer, Online Manager, On-Site Manager and Super Admin roles.
- Customer registration, password policy, one-time email OTP, and identity-document upload for manager review. Documents are stored outside `static/`.
- SMTP email OTP when SMTP environment variables are configured; otherwise the development OTP is printed in the application console.
- Optional Google reCAPTCHA v2 pattern. Configure `RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET_KEY`, then set `CAPTCHA_ENFORCE=true`.
- Customer reservation requests, date-overlap check, full-payment simulation, reservation status and cancellation request.
- Super Admin manager-account creation/disable, customer approval and identity-document review.
- Shared manager room create/activate, reservation-history view, customer-report replies, payment-record view and CSV export. Online Manager-only reservation approve/reject; On-Site Manager-only walk-in creation and full-payment recording.
- Every-minute expiry job for unpaid/paid pending reservations that reach their check-in date without manager approval.
- Hotel assistant with optional Groq API integration using `GROQ_API_KEY`; without a key it displays a basic fallback response.
- Responsive CSS and image assets adapted from the uploaded reference project.

## Critical limitations
- **Payments are simulated only.** No card is charged and no money is transferred. A real payment gateway requires merchant credentials, signed webhooks, provider verification, secure deployment and refund endpoints. Do not enter real card details.
- **Identity review is manual.** The app stores an uploaded document privately for authorized manager review; it does not validate government-ID authenticity through an external identity provider.
- **OTP local fallback is for development only.** Configure SMTP before testing real email delivery. Add rate limiting, abuse prevention and delivery monitoring before deployment.
- **Chatbot is not a full autonomous hotel agent.** It uses a configured Groq-compatible endpoint when a key is provided, otherwise it provides a safe fallback. Review privacy and prompt controls before production.
- **Refunds are not executed.** The payment table is a development record only.
- **This is not production-ready.** Authorization boundaries, file access, booking concurrency/locking, audit logs, privacy controls, backup/restore and end-to-end tests need a security review.

## Role boundaries
- Customer: own reservations, simulated payment, cancellation requests, customer reports.
- Online Manager: shared management pages and online reservation decisions.
- On-Site Manager: shared management pages plus exclusive walk-in booking and full-payment recording (simulated).
- Super Admin: customer/identity review and manager account creation/disable.

Both manager roles share room, reservation-history, report/export and payment-record viewing routes. A production build should further restrict specific actions and add audit logging.

The first startup seeds 12 sample rooms across Standard, Deluxe, Suite and Family categories. These are demonstration inventory and sample PHP rates; edit them in Room Management.

## Optional environment variables
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` — real email OTP delivery.
- `RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET_KEY`, `CAPTCHA_ENFORCE=true` — enforce Google reCAPTCHA v2 on login and registration.
- `GROQ_API_KEY` — enable the optional Groq-powered assistant.
