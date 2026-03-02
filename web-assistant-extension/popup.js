let selectedElement = null;

document.getElementById('scanBtn').onclick = () => {
  const status = document.getElementById('status');
  status.textContent = 'Scanning...';

  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    chrome.tabs.sendMessage(tabs[0].id, { action: 'get_elements' }, (response) => {
      if (response && response.elements) {
        displayElements(response.elements);
        status.textContent = `Found ${response.elements.length} interactive elements.`;
      } else {
        status.textContent = 'Failed to scan. Try refreshing the page.';
      }
    });
  });
};

function displayElements(elements) {
  const list = document.getElementById('element-list');
  list.innerHTML = ''; // Clear previous

  elements.forEach((el) => {
    const item = document.createElement('div');
    item.className = 'element-item';
    item.textContent = `[${el.tagName}] - ${el.label}`;
    item.onclick = () => {
      // De-select previous
      const prev = document.querySelector('.selected');
      if (prev) prev.classList.remove('selected');

      // Select current
      item.classList.add('selected');
      item.style.backgroundColor = '#d1e7ff';
      selectedElement = el;
      document.getElementById('status').textContent = `Selected: ${el.label}`;
    };
    list.appendChild(item);
  });
}

document.getElementById('actionBtn').onclick = () => {
  if (!selectedElement) {
    alert('Please select an element first!');
    return;
  }

  const textToType = document.getElementById('actionInput').value;
  const action = textToType ? 'type' : 'click';

  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    chrome.tabs.sendMessage(tabs[0].id, {
      action: 'perform_action',
      task: action,
      agentId: selectedElement.agentId,
      text: textToType
    }, (response) => {
      if (response && response.success) {
        document.getElementById('status').textContent = `Action successful!`;
      } else {
        document.getElementById('status').textContent = `Error: ${response.error || 'unknown'}`;
      }
    });
  });
};