// background.js is minimal for now as we're doing the logic in the popup for manual control
chrome.runtime.onInstalled.addListener(() => {
  console.log('Web Agent Extension Installed');
});