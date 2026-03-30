const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

function safeUnlink(filePath) {
  try {
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath);
    }
  } catch (_err) {
  }
}

function safeSymlink(target, linkPath) {
  try {
    safeUnlink(linkPath);
    fs.symlinkSync(target, linkPath);
    return true;
  } catch (_err) {
    return false;
  }
}

function commandExists(cmd) {
  const checker = process.platform === 'win32' ? 'where' : 'which';
  const result = spawnSync(checker, [cmd], { stdio: 'ignore' });
  return result.status === 0;
}

function getNpmGlobalBin() {
  const result = spawnSync('npm', ['bin', '-g'], { encoding: 'utf8' });
  if (result.status !== 0) {
    return '';
  }
  return String(result.stdout || '').trim();
}

function ensureDir(dirPath) {
  try {
    if (!fs.existsSync(dirPath)) {
      fs.mkdirSync(dirPath, { recursive: true });
    }
    return true;
  } catch (_err) {
    return false;
  }
}

function installGlobalCapacitor() {
  const result = spawnSync('npm', ['install', '-g', '@capacitor/cli@8.3.0', '--quiet'], {
    stdio: 'inherit'
  });
  return result.status === 0;
}

function main() {
  if (process.platform === 'win32') {
    return;
  }

  if (commandExists('cap') || commandExists('capacitor')) {
    console.log('[ensure-cap-bin] cap command already available');
    return;
  }

  const root = process.cwd();
  const capBin = path.join(root, 'node_modules', '.bin', 'cap');
  const capacitorBin = path.join(root, 'node_modules', '.bin', 'capacitor');

  if (!fs.existsSync(capBin) && !fs.existsSync(capacitorBin)) {
    return;
  }

  const targets = ['/usr/local/bin', '/usr/bin'];
  const npmGlobalBin = getNpmGlobalBin();
  if (npmGlobalBin) {
    targets.unshift(npmGlobalBin);
  }

  const localHomeBin = process.env.HOME ? path.join(process.env.HOME, '.npm-global', 'bin') : '';
  if (localHomeBin) {
    targets.unshift(localHomeBin);
  }

  for (const dir of targets) {
    if (!ensureDir(dir)) {
      continue;
    }

    if (fs.existsSync(capBin)) {
      safeSymlink(capBin, path.join(dir, 'cap'));
    }
    if (fs.existsSync(capacitorBin)) {
      safeSymlink(capacitorBin, path.join(dir, 'capacitor'));
    }
  }

  if (commandExists('cap') || commandExists('capacitor')) {
    console.log('[ensure-cap-bin] cap command linked successfully');
    return;
  }

  console.log('[ensure-cap-bin] trying npm global install for @capacitor/cli');
  installGlobalCapacitor();

  if (commandExists('cap') || commandExists('capacitor')) {
    console.log('[ensure-cap-bin] cap command available after global install');
  } else {
    console.log('[ensure-cap-bin] WARNING: cap command still unavailable');
  }
}

main();
