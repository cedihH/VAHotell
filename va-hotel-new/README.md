# VA Hotel — new local project

This is a Spring Boot + Java 21 + Maven + SQLite project. It includes a four-portal entry page, separate role dashboards, room inventory, reservation/payment simulations, identity review, and customer support.

## Start the app

In GitHub Codespaces, open the repository in a Codespace. Port 8080 is forwarded and the app starts automatically. On a local computer with Java 21+ and Maven installed, run `./run.sh` on macOS/Linux or double-click `run.bat` on Windows:
Then open the forwarded port 8080 URL in Codespaces (or `http://localhost:8080` locally). No SMTP or payment credentials are required for the demo. CAPTCHA activates automatically only when both Google keys are present; the assistant falls back to database-backed room FAQs without a Groq key. SQLite is stored under `./data/va-hotel.db`.

The login page does not display demo credentials. Development-only starter accounts may be created by the bootstrap service unless `VA_HOTEL_BOOTSTRAP_DEMO_ACCOUNTS=false` is set. The app fills missing sample room records on startup, including when an existing SQLite file contains only some rooms.

Reservation confirmations, simulated payment receipts, and the customer dashboard's **Email reservation proof** action are sent to the account email. For Gmail, configure Codespaces secrets `MAIL_USERNAME` (the Gmail address) and `MAIL_PASSWORD` (a Google app password); Gmail SMTP host and port default to `smtp.gmail.com:587`. Other SMTP providers can override these with `SMTP_HOST` and `SMTP_PORT`. Without SMTP credentials, reservations still work and the app clearly reports that email was not sent. New customer registrations work without SMTP; their one-time code appears in the app log.

These are local demo credentials. For a deployment, set `VA_HOTEL_BOOTSTRAP_DEMO_ACCOUNTS=false` and `VA_HOTEL_SHOW_DEMO_CREDENTIALS=false`, then create private accounts through the admin workflow. Existing demo accounts in an SQLite database are not removed by these settings.

## Included starter workflows
- Public home and room browsing pages, with manager-maintained room inventory.
- Database-backed login with BCrypt and separate Customer, Online Manager, On-Site Manager and Super Admin roles.
- Customer registration, password policy, one-time email OTP, and identity-document upload for manager review. Documents are stored outside `static/`.
- SMTP email OTP when SMTP environment variables are configured; otherwise the development OTP is printed in the application console.
- Google reCAPTCHA v2 is enabled when both `RECAPTCHA_SITE_KEY` and `RECAPTCHA_SECRET_KEY` are set. Set `CAPTCHA_ENFORCE=false` to disable it locally.
- Customer reservation requests, date-overlap check, full-payment simulation, reservation status and cancellation request.
- Super Admin manager-account creation/disable, customer approval and identity-document review.
- Shared manager room create/activate, reservation-history view, customer-report replies, payment-record view and CSV export. Online Manager-only reservation approve/reject; On-Site Manager-only walk-in creation and full-payment recording.
- Every-minute expiry job for unpaid/paid pending reservations that reach their check-in date without manager approval.
- Hotel assistant with optional Groq API integration using `GROQ_API_KEY`; without a key it displays a basic fallback response.
- Responsive VA blue, orange, and white styling. Room photos are in `src/main/resources/static/images/rooms/`.
- No hotel facade/background image from the prior conversation was present in the repository. To use it as the portal background, add the Vision Academy building image as `src/main/resources/static/images/vision-academy-building.jpg`.

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

Startup ensures 12 sample rooms across Standard, Deluxe, Suite and Family categories exist. These are demonstration inventory and sample PHP rates; edit them in Room Management.

## Optional environment variables
- `MAIL_USERNAME`, `MAIL_PASSWORD` — Gmail address and Google app password for OTP, reservation confirmations and payment proofs. Gmail SMTP host and port are defaults; for another provider override with `SMTP_HOST` and `SMTP_PORT`. `MAIL_NAME` and legacy `SMTP_USERNAME` / `SMTP_PASSWORD` are also accepted; optional `SMTP_FROM` controls the sender address. Store credentials as Codespaces secrets; do not commit them.
- `RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET_KEY`, optional `CAPTCHA_ENFORCE=false` — show and verify Google reCAPTCHA v2 on login and registration when both keys are configured.
- `GROQ_API_KEY` — enable the optional Groq-powered assistant.
