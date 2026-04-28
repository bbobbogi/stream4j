const GROUP_ID = 'io.github.bbobbogi';
const ARTIFACT_ID = 'stream4j';
const FALLBACK_VERSION = '1.0.1';
const MAVEN_METADATA_URL = `https://repo1.maven.org/maven2/${GROUP_ID.replace(
  /\./g,
  '/',
)}/${ARTIFACT_ID}/maven-metadata.xml`;

async function fetchMavenCentralRelease(): Promise<string | null> {
  try {
    const res = await fetch(MAVEN_METADATA_URL, {
      signal: AbortSignal.timeout(5000),
      headers: { Accept: 'application/xml' },
    });
    if (!res.ok) return null;
    const xml = await res.text();
    return xml.match(/<release>([^<]+)<\/release>/)?.[1]?.trim() || null;
  } catch {
    return null;
  }
}

const envVersion = process.env['VERSION'] ?? process.env['STREAM4J_VERSION'];
const mavenVersion = envVersion ? null : await fetchMavenCentralRelease();

export const INSTALL_VERSION = envVersion ?? mavenVersion ?? FALLBACK_VERSION;

export const LIBRARY_VERSION = INSTALL_VERSION.replace(/-SNAPSHOT$/, '');

export const COORDINATES = `${GROUP_ID}:${ARTIFACT_ID}:${INSTALL_VERSION}`;

export const GITHUB_RELEASES_API = `https://api.github.com/repos/bbobbogi/${ARTIFACT_ID}/releases/latest`;
