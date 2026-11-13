(() => {
  const panelPath = /^\/(customer|super-admin|onsite-manager|online-manager)\/dashboard\/?$/;
  let toastTimer;

  function toast(message, kind = "info") {
    let node = document.querySelector(".va-toast");
    if (!node) {
      node = document.createElement("div");
      node.className = "va-toast";
      node.setAttribute("role", "status");
      node.setAttribute("aria-live", "polite");
      document.body.append(node);
    }
    node.dataset.kind = kind;
    node.textContent = message;
    node.classList.add("visible");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => node.classList.remove("visible"), 5000);
  }

  function modal(title, message, confirmLabel, onConfirm) {
    const dialog = document.createElement("dialog");
    dialog.className = "va-modal";
    const heading = document.createElement("h2");
    heading.textContent = title;
    const body = document.createElement("p");
    body.textContent = message;
    const actions = document.createElement("div");
    actions.className = "va-modal-actions";
    const cancel = document.createElement("button");
    cancel.type = "button";
    cancel.className = "button secondary";
    cancel.textContent = "Stay here";
    const confirm = document.createElement("button");
    confirm.type = "button";
    confirm.className = "button";
    confirm.textContent = confirmLabel;
    actions.append(cancel, confirm);
    dialog.append(heading, body, actions);
    document.body.append(dialog);
    cancel.addEventListener("click", () => dialog.close());
    confirm.addEventListener("click", () => {
      dialog.close();
      onConfirm();
    });
    dialog.addEventListener("close", () => dialog.remove(), { once: true });
    dialog.showModal();
  }

  const notificationsDialog = document.querySelector("[data-notifications-dialog]");
  document.querySelector("[data-notifications-open]")?.addEventListener("click", () => notificationsDialog?.showModal());
  document.querySelector("[data-notifications-close]")?.addEventListener("click", () => notificationsDialog?.close());
  notificationsDialog?.addEventListener("click", event => {
    if (event.target === notificationsDialog) notificationsDialog.close();
  });
  if (notificationsDialog?.dataset.openOnLoad === "true") notificationsDialog.showModal();

  document.querySelectorAll("[data-review-open]").forEach(button => {
    button.addEventListener("click", () => {
      const dialog = document.getElementById(button.dataset.reviewTarget);
      if (dialog instanceof HTMLDialogElement && !dialog.open) dialog.showModal();
    });
  });
  document.querySelectorAll("[data-review-close]").forEach(button => {
    button.addEventListener("click", () => button.closest("dialog")?.close());
  });
  document.querySelectorAll("[data-customer-review-dialog]").forEach(dialog => {
    dialog.addEventListener("click", event => {
      if (event.target === dialog) dialog.close();
    });
    if (dialog.dataset.openOnLoad === "true") dialog.showModal();
  });

  function passwordEyeIcon(visible) {
    const ns = "http://www.w3.org/2000/svg";
    const svg = document.createElementNS(ns, "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("aria-hidden", "true");
    const outline = document.createElementNS(ns, "path");
    outline.setAttribute("d", "M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z");
    outline.setAttribute("fill", "none");
    outline.setAttribute("stroke", "currentColor");
    outline.setAttribute("stroke-width", "1.8");
    svg.append(outline);
    const pupil = document.createElementNS(ns, "circle");
    pupil.setAttribute("cx", "12"); pupil.setAttribute("cy", "12"); pupil.setAttribute("r", "3");
    pupil.setAttribute("fill", "none"); pupil.setAttribute("stroke", "currentColor"); pupil.setAttribute("stroke-width", "1.8");
    svg.append(pupil);
    if (!visible) {
      const slash = document.createElementNS(ns, "path");
      slash.setAttribute("d", "m3 3 18 18"); slash.setAttribute("fill", "none");
      slash.setAttribute("stroke", "currentColor"); slash.setAttribute("stroke-width", "1.8");
      svg.append(slash);
    }
    return svg;
  }

  function labelTextFor(field) {
    let label = field.closest("label");
    if (!label && field.id) label = [...document.querySelectorAll("label[for]")].find(item => item.htmlFor === field.id);
    if (label) {
      label = label.cloneNode(true);
      label.querySelectorAll("input,button,.staff-field-hint,.field-hint").forEach(item => item.remove());
      const text = label.textContent.replace(/\s+/g, " ").trim().replace(/[:*]+$/, "").trim();
      if (text) return text;
    }
    return (field.getAttribute("aria-label") || field.name || field.id || "value")
      .replace(/([a-z])([A-Z])/g, "$1 $2").replace(/[-_]+/g, " ").trim();
  }

  function samplePlaceholder(field, label) {
    const key = `${field.name || ""} ${label}`.toLowerCase();
    if (field.type === "search" || /search|keyword/.test(key)) return "e.g. Room 201, Deluxe, or quiet room";
    if (/email/.test(key)) return "e.g. maria@example.com";
    if (/password/.test(key)) return "Create a strong password";
    if (/first name|given name/.test(key)) return "e.g. Maria";
    if (/surname|last name|family name/.test(key)) return "e.g. Santos";
    if (/full name|guest name|customer name|\bname\b/.test(key)) return "e.g. Maria Santos";
    if (/verification code|otp|\bcode\b/.test(key)) return "e.g. 123456";
    if (/room number/.test(key)) return "e.g. 501";
    if (/room type|category/.test(key)) return "e.g. Deluxe";
    if (/nightly price|refund amount|\bamount\b|\bprice\b/.test(key)) return "e.g. 2500.00";
    if (/subject/.test(key)) return "e.g. Question about my booking";
    if (/description/.test(key)) return "e.g. Comfortable room with Wi-Fi and air conditioning";
    if (/message|reply|response|comment/.test(key)) return "e.g. Please let me know how I can help";
    if (/question/.test(key)) return "e.g. What room types can I book?";
    if (/phone|mobile|telephone/.test(key)) return "e.g. 09171234567";
    if (/address/.test(key)) return "e.g. 123 Mabini Street, Manila";
    return field instanceof HTMLTextAreaElement ? "e.g. Please let me know how I can help" : "e.g. Sample value";
  }

  document.querySelectorAll('input:not([type]),input[type="text"],input[type="email"],input[type="password"],input[type="search"],input[type="tel"],input[type="url"],input[type="number"],textarea').forEach(field => {
    if (field.hasAttribute("placeholder")) return;
    const label = labelTextFor(field);
    field.placeholder = samplePlaceholder(field, label);
  });

  document.querySelectorAll('input[type="password"]').forEach(input => {
    if (input.dataset.hasVisibilityToggle === "true") return;
    input.dataset.hasVisibilityToggle = "true";
    const wrapper = document.createElement("span");
    wrapper.className = "password-field-wrap";
    input.parentNode.insertBefore(wrapper, input);
    wrapper.append(input);
    const toggle = document.createElement("button");
    toggle.type = "button";
    toggle.className = "password-visibility-toggle";
    toggle.setAttribute("aria-label", "Show password");
    toggle.title = "Show password";
    toggle.append(passwordEyeIcon(false));
    input.insertAdjacentElement("afterend", toggle);
    toggle.addEventListener("click", () => {
      const visible = input.type === "password";
      input.type = visible ? "text" : "password";
      toggle.setAttribute("aria-label", visible ? "Hide password" : "Show password");
      toggle.title = visible ? "Hide password" : "Show password";
      toggle.replaceChildren(passwordEyeIcon(visible));
      input.focus();
    });
  });

  document.querySelectorAll("[data-date-range]").forEach(form => {
    const checkIn = form.querySelector('input[name="checkIn"]');
    const checkOut = form.querySelector('input[name="checkOut"]');
    if (!checkIn || !checkOut) return;
    const minimumCheckIn = form.dataset.minCheckIn || checkIn.min;
    const minimumCheckOut = form.dataset.minCheckOut || checkOut.min;
    if (minimumCheckIn) checkIn.min = minimumCheckIn;
    const nextDay = value => {
      const [year, month, day] = value.split("-").map(Number);
      const date = new Date(Date.UTC(year, month - 1, day + 1));
      return date.toISOString().slice(0, 10);
    };
    const syncCheckoutDate = () => {
      const afterCheckIn = checkIn.value ? nextDay(checkIn.value) : minimumCheckOut;
      checkOut.min = minimumCheckOut && afterCheckIn
        ? (afterCheckIn > minimumCheckOut ? afterCheckIn : minimumCheckOut)
        : (afterCheckIn || "");
      if (checkOut.value && checkOut.min && checkOut.value < checkOut.min) checkOut.value = "";
    };
    checkIn.addEventListener("change", syncCheckoutDate);
    syncCheckoutDate();
  });

  const signupPassword = document.querySelector("[data-password-check]");
  if (signupPassword) {
    const requirements = [
      ["length", value => value.length >= 10],
      ["upper", value => /[A-Z]/.test(value)],
      ["lower", value => /[a-z]/.test(value)],
      ["number", value => /\d/.test(value)],
      ["special", value => /[^A-Za-z0-9]/.test(value)],
      ["spaces", value => !/\s/.test(value)]
    ];
    const updatePasswordChecks = () => {
      requirements.forEach(([key, check]) => {
        const item = signupPassword.querySelector(`[data-requirement="${key}"]`);
        const valid = check(signupPassword.value);
        item?.classList.toggle("is-met", valid);
        item?.setAttribute("aria-checked", String(valid));
      });
      signupPassword.setCustomValidity(requirements.every(([, check]) => check(signupPassword.value)) ? "" : "Complete all password requirements.");
    };
    signupPassword.addEventListener("input", updatePasswordChecks);
    updatePasswordChecks();
  }

  document.querySelectorAll("[data-policy-open]").forEach(button => button.addEventListener("click", () => {
    modal("VA Hotel guest policies", "ACCOUNT REVIEW\nVerify your email and submit a valid photo ID. Hotel management reviews new customer accounts before activation.\n\nRESERVATIONS\nA booking request is subject to availability and management approval. Check your dashboard for its current status.\n\nPAYMENTS\nPayments on this website are simulations only; no real money is collected. A 50% deposit is requested first, with the remaining balance due later when shown in your dashboard.\n\nCHANGES AND CANCELLATIONS\nSubmit reservation changes or cancellation requests through your dashboard. They take effect after management review.\n\nPRIVACY\nIdentity documents and account details are available only to authorized hotel staff for account review and service operations.", "Got it", () => {});
  }));

  document.querySelectorAll("[data-file-name]").forEach(status => {
    const fileInput = status.closest("label")?.querySelector('input[type="file"]');
    fileInput?.addEventListener("change", () => {
      status.textContent = fileInput.files?.[0]?.name || "Choose a file from your device";
      status.classList.toggle("has-file", Boolean(fileInput.files?.length));
    });
  });

  const chatPanel = document.querySelector("[data-chat-panel]");
  const chatToggle = document.querySelector("[data-chat-toggle]");
  const chatLog = document.querySelector("[data-chat-messages]");
  const chatForm = document.querySelector("[data-chat-form]");
  function setChatOpen(open) {
    if (!chatPanel || !chatToggle) return;
    chatPanel.hidden = !open;
    chatToggle.setAttribute("aria-expanded", String(open));
    if (open) chatPanel.querySelector("textarea")?.focus();
  }
  function addChatMessage(text, kind) {
    const message = document.createElement("p");
    message.className = `chat-message ${kind}-message`;
    message.textContent = text;
    chatLog?.append(message);
    if (chatLog) chatLog.scrollTop = chatLog.scrollHeight;
  }
  chatToggle?.addEventListener("click", () => setChatOpen(chatPanel.hidden));
  document.querySelector("[data-chat-close]")?.addEventListener("click", () => setChatOpen(false));
  document.querySelectorAll("[data-chat-question]").forEach(button => button.addEventListener("click", () => {
    const input = chatForm?.querySelector("textarea[name=question]");
    if (input) { input.value = button.dataset.chatQuestion; chatForm.requestSubmit(); }
  }));
  chatForm?.addEventListener("submit", async event => {
    event.preventDefault();
    const input = chatForm.querySelector("textarea[name=question]");
    const question = input?.value.trim();
    if (!question) return;
    addChatMessage(question, "user");
    chatForm.querySelector("button[type=submit]").disabled = true;
    try {
      const response = await fetch(chatForm.action, { method: "POST", body: new FormData(chatForm), credentials: "same-origin" });
      const html = await response.text();
      const result = new DOMParser().parseFromString(html, "text/html");
      const answer = result.querySelector(".assistant-answer p")?.textContent?.trim()
        || result.querySelector(".alert.error")?.textContent?.trim()
        || "I could not answer that just now. Please contact hotel management.";
      addChatMessage(answer, "assistant");
      input.value = "";
    } catch {
      addChatMessage("I could not connect to the hotel assistant. Please try again.", "assistant");
    } finally {
      chatForm.querySelector("button[type=submit]").disabled = false;
      input?.focus();
    }
  });
  if (location.hash === "#hotel-assistant") setChatOpen(true);

  function labelFor(form) {
    const submitter = form.__vaSubmitter || form.querySelector("button[type=submit],button:not([type]),input[type=submit]");
    const label = (submitter?.innerText || submitter?.value || form.getAttribute("aria-label") || "Action").trim();
    return label.replace(/\s+/g, " ").slice(0, 100);
  }

  document.addEventListener("click", event => {
    const button = event.target.closest("button[type=submit],button:not([type]),input[type=submit]");
    if (button?.form) button.form.__vaSubmitter = button;
  }, true);

  document.addEventListener("submit", event => {
    const form = event.target;
    if (!(form instanceof HTMLFormElement) || form.dataset.noActionToast === "true") return;
    const label = labelFor(form);
    const decisionAction = /^(approve|reject|decline|keep|confirm|cancel|request|disable|enable|archive|restore|delete|check in|complete checkout|record|refund|resolve|deny|revoke|assign room|send reply)/i.test(label);
    const confirmation = form.dataset.confirm || (decisionAction ? `Are you sure you want to ${label.toLowerCase()}?` : "");
    if (confirmation && form.dataset.confirmed !== "true") {
      event.preventDefault();
      event.stopImmediatePropagation();
      const submitter = form.__vaSubmitter;
      modal("Please confirm", confirmation, submitter?.innerText?.trim() || "Confirm", () => {
        form.dataset.confirmed = "true";
        if (submitter) form.requestSubmit(submitter); else form.requestSubmit();
      });
      return;
    }
    if (!form.action.endsWith("/logout")) sessionStorage.removeItem("vaHotelBackGuardPath");
    sessionStorage.setItem("vaHotelPendingAction", JSON.stringify({
      kind: "action",
      label,
      path: location.pathname,
      snapshot: document.body.innerText.slice(0, 10000),
      time: Date.now()
    }));
  }, true);

  document.addEventListener("click", event => {
    const link = event.target.closest("a[href]");
    if (!link) return;
    const url = new URL(link.href, location.href);
    if (link.hasAttribute("download") || /\.csv$/i.test(url.pathname)) {
      toast(`Downloading ${link.innerText.trim() || "file"}.`);
      return;
    }
    if (link.target === "_blank") {
      if (url.origin === location.origin) toast(`Opening ${link.innerText.trim() || "page"} in a new tab.`);
      return;
    }
    if (url.origin !== location.origin || url.pathname === location.pathname || link.hash) return;
    sessionStorage.removeItem("vaHotelBackGuardPath");
    sessionStorage.setItem("vaHotelPendingAction", JSON.stringify({
      kind: "navigation",
      label: `Open ${link.innerText.trim() || "page"}`,
      path: location.pathname,
      snapshot: document.body.innerText.slice(0, 10000),
      time: Date.now()
    }));
  }, true);

  window.addEventListener("pageshow", () => {
    const raw = sessionStorage.getItem("vaHotelPendingAction");
    if (raw) {
      sessionStorage.removeItem("vaHotelPendingAction");
      try {
        const pending = JSON.parse(raw);
        if (Date.now() - pending.time < 60000) {
          const feedback = document.querySelector(".alert.error, .alert.success, [role=alert]");
          const result = feedback?.innerText.trim();
          const fallback = pending.kind === "navigation"
            ? `${pending.label.replace(/^Open\s+/, "")} opened.`
            : `${pending.label} completed.`;
          toast(result || fallback, feedback?.classList.contains("error") ? "error" : "success");
        }
      } catch { /* Ignore malformed tab-local state. */ }
    }

    if (panelPath.test(location.pathname)) {
      sessionStorage.setItem("vaHotelBackGuardPath", location.pathname);
      if (!history.state?.vaHotelBackGuard) history.pushState({ vaHotelBackGuard: true }, "", location.href);
    } else {
      sessionStorage.removeItem("vaHotelBackGuardPath");
    }
  });

  window.addEventListener("popstate", () => {
    const panel = sessionStorage.getItem("vaHotelBackGuardPath");
    if (!panel) return;
    history.pushState({ vaHotelBackGuard: true }, "", panel);
    if (document.querySelector("dialog.va-modal[open]")) return;
    modal("Sign out?", "Going back from your main panel will sign you out. Do you want to continue?", "Sign out", () => {
      const logout = document.querySelector('form[action$="/logout"]');
      if (logout) logout.requestSubmit();
      else {
        const csrf = document.querySelector('input[name="_csrf"]')?.value;
        const form = document.createElement("form");
        form.method = "post";
        form.action = "/logout";
        if (csrf) {
          const input = document.createElement("input");
          input.type = "hidden";
          input.name = "_csrf";
          input.value = csrf;
          form.append(input);
        }
        document.body.append(form);
        form.submit();
      }
    });
  });

  document.addEventListener("click", event => {
    const printButton = event.target.closest("[data-print-page]");
    if (printButton) {
      toast("Opening the print dialog.");
      setTimeout(() => window.print(), 250);
    }
  });
})();

// Keep simulated payment examples visibly fictional and validate the selected format.
document.querySelectorAll("[data-payment-method]").forEach(select => {
  const form = select.closest("form");
  const input = form?.querySelector("[data-payment-number]");
  const label = form?.querySelector("[data-payment-label]");
  const update = () => {
    const gcash = select.value === "SIMULATED_GCASH";
    if (label) label.textContent = gcash ? "Sample GCash number" : "Sample Visa number";
    if (input) {
      input.maxLength = gcash ? 11 : 16;
      input.pattern = gcash ? "[0-9]{11}" : "[0-9]{16}";
      input.placeholder = gcash ? "09171234567" : "4111111111111111";
      input.setCustomValidity("");
    }
  };
  select.addEventListener("change", update);
  input?.addEventListener("input", () => input.setCustomValidity(""));
  update();
});

(() => {
  const grid = document.querySelector("[data-room-grid]");
  if (!grid) return;
  const listings = [...grid.querySelectorAll("[data-room-listing]")];
  const search = document.querySelector("[data-room-search]");
  const category = document.querySelector("[data-room-category]");
  const sort = document.querySelector("[data-room-sort]");
  const availability = document.querySelector("[data-room-availability]");
  const count = document.querySelector("[data-room-count]");
  const empty = document.querySelector("[data-room-empty]");

  if (availability?.dataset.datesValid !== "true") {
    availability?.querySelectorAll('option[value="available"],option[value="unavailable"]').forEach(option => option.disabled = true);
  }

  function updateRooms() {
    const query = (search?.value || "").trim().toLowerCase();
    const selectedCategory = category?.value || "";
    const selectedAvailability = availability?.value || "all";
    const mode = sort?.value || "recommended";
    listings.sort((a, b) => {
      const roomCompare = a.dataset.room.localeCompare(b.dataset.room, undefined, { numeric: true });
      if (mode === "price-asc") return Number(a.dataset.price) - Number(b.dataset.price) || roomCompare;
      if (mode === "price-desc") return Number(b.dataset.price) - Number(a.dataset.price) || roomCompare;
      if (mode === "category") return a.dataset.category.localeCompare(b.dataset.category) || roomCompare;
      return roomCompare;
    });
    listings.forEach(item => {
      const availabilityMatch = selectedAvailability === "all" || item.dataset.available === (selectedAvailability === "available" ? "true" : "false");
      const matches = (!selectedCategory || item.dataset.category === selectedCategory)
        && (!query || item.dataset.search.toLowerCase().includes(query)) && availabilityMatch;
      item.hidden = !matches;
      grid.append(item);
    });
    const visibleCount = listings.filter(item => !item.hidden).length;
    if (count) count.textContent = String(visibleCount);
    if (empty) empty.hidden = visibleCount !== 0;
  }
  [search, category, sort, availability].forEach(control => control?.addEventListener(control === search ? "input" : "change", updateRooms));
  updateRooms();

  function openDetails(listing) {
    const dialog = listing?.querySelector("[data-room-detail-dialog]");
    if (dialog && !dialog.open) dialog.showModal();
  }
  grid.addEventListener("click", event => {
    const trigger = event.target.closest(".room-card-trigger");
    if (trigger) openDetails(trigger.closest("[data-room-listing]"));
  });
  document.querySelectorAll("[data-room-detail-dialog]").forEach(dialog => {
    dialog.querySelector("[data-room-close]")?.addEventListener("click", () => dialog.close());
    dialog.addEventListener("click", event => {
      if (event.target === dialog) dialog.close();
    });
    if (dialog.dataset.openOnLoad === "true") dialog.showModal();
  });
})();
