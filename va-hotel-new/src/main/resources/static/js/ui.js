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
