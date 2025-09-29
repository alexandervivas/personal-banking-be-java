(function(){
  function init(){
    if (window.mermaid && typeof window.mermaid.initialize === 'function') {
      window.mermaid.initialize({ startOnLoad: true });
    }
  }
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
