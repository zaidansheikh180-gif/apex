// content.js
function getInteractiveElements() {
  const interactiveSelector = 'a, button, input, select, textarea, [role="button"], [role="link"]';
  const elements = Array.from(document.querySelectorAll(interactiveSelector));

  return elements.map((el, index) => {
    // Assign a temporary ID for the session if it doesn't have one
    const agentId = `agent-el-${index}`;
    el.setAttribute('data-agent-id', agentId);

    // Get the most descriptive label possible
    const label = el.innerText || el.ariaLabel || el.placeholder || el.title || el.value || "unlabeled";
    const rect = el.getBoundingClientRect();

    return {
      agentId: agentId,
      tagName: el.tagName,
      type: el.type || null,
      label: label.trim().substring(0, 50),
      isVisible: rect.width > 0 && rect.height > 0 && getComputedStyle(el).display !== 'none'
    };
  }).filter(el => el.isVisible);
}

// Listen for commands from the background script
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "get_elements") {
    sendResponse({ elements: getInteractiveElements() });
  } else if (request.action === "perform_action") {
    const el = document.querySelector(`[data-agent-id="${request.agentId}"]`);
    if (el) {
      if (request.task === "click") {
        el.click();
      } else if (request.task === "type") {
        el.value = request.text;
        el.dispatchEvent(new Event('input', { bubbles: true }));
        el.dispatchEvent(new Event('change', { bubbles: true }));
      }
      sendResponse({ success: true });
    } else {
      sendResponse({ success: false, error: "Element not found" });
    }
  }
});