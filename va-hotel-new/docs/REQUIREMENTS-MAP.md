# VA Hotel feature map — guided by uploaded Matcha Lover Hotel project

| Function | Current starter status |
|---|---|
| Customer registration | Basic workflow implemented |
| Email OTP verification | Implemented; SMTP optional, console fallback for local development |
| Identity verification | Private document upload and manual review; no external authenticity provider |
| Customer dashboard | Basic reservation and report page |
| Online Manager dashboard | Basic shared management dashboard; role protected |
| On-Site Manager dashboard | Shared management functions plus On-Site-only walk-in booking and simulated full-payment recording |
| Super Admin dashboard | Customer/identity approval and manager account create/disable |
| Approve/reject reservations | Online Manager-only approve/reject; paid bookings required for approval |
| Cancellation/refund processing | Cancellation request and manager confirmation; actual refunds not implemented |
| Reservation expiry automation | Every-minute job expires unpaid/paid pending bookings at check-in date if not manager-approved |
| Room availability management | Create/edit/activate/deactivate rooms and date overlap check |
| Reports and export | Reservation and payment/revenue CSV export; manager guest-report replies |
| AI chatbot | Optional Groq-compatible endpoint; fallback without key |
| Real payment gateway | Not implemented; full payment is simulated only |

## Payment rule
VA Hotel reservation payments are full-payment-only in this starter. No down-payment or balance-payment option is provided. Walk-in full payment is recorded by the On-Site Manager in a dedicated workflow, but this is still a simulation in the local starter.

## Guide-derived structure
The reference project informed the MVC/Thymeleaf structure, room browsing, registration and login patterns, CAPTCHA integration pattern, role-protected manager pages, customer reservation views, responsive static assets, and chatbot approach.
