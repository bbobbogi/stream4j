export const GITHUB_RELEASES_API =
  'https://api.github.com/repos/bbobbogi/stream4j/releases/latest';

export const VERSION_UPDATER_SCRIPT = `
(function(){
  if (window.__stream4jVersionFetched) return;
  window.__stream4jVersionFetched = true;
  fetch('${GITHUB_RELEASES_API}', { headers: { Accept: 'application/vnd.github+json' } })
    .then(function(r){ return r.ok ? r.json() : null; })
    .then(function(d){
      if (!d || !d.tag_name) return;
      var v = String(d.tag_name).replace(/^v/, '');
      document.querySelectorAll('[data-stream4j-version]').forEach(function(el){
        el.textContent = 'v' + v;
      });
      document.querySelectorAll('[data-stream4j-install-version]').forEach(function(el){
        el.textContent = v;
      });
    })
    .catch(function(){});
})();
`.trim();
