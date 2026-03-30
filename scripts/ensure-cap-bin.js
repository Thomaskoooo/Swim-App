const fs = require('fs');
const path = require('path');

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

function main() {
  if (process.platform === 'win32') {
    return;
  }

  const root = process.cwd();
  const capBin = path.join(root, 'node_modules', '.bin', 'cap');
  const capacitorBin = path.join(root, 'node_modules', '.bin', 'capacitor');

  if (!fs.existsSync(capBin) && !fs.existsSync(capacitorBin)) {
    return;
  }

  const targets = ['/usr/local/bin', '/usr/bin'];
  for (const dir of targets) {
    if (!fs.existsSync(dir)) {
      continue;
    }

    if (fs.existsSync(capBin)) {
      safeSymlink(capBin, path.join(dir, 'cap'));
    }
    if (fs.existsSync(capacitorBin)) {
      safeSymlink(capacitorBin, path.join(dir, 'capacitor'));
    }
  }
}

main();
