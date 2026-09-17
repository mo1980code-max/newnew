#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Static Java symbol verifier for the app module.

    python3 tools/verify_java_symbols.py

There is no JDK (and no network to fetch one) in this sandbox, so javac cannot run here. This
script therefore does the next best thing: it builds a symbol table out of every .java file in
`app/src/main/java` and checks that every reference the app makes **to its own code** resolves.

  1. every `import org.Allah_Clock_Live_Wallpaper.…` names a type that exists (top level or
     nested) - a moved or deleted class is the usual cause of "cannot find symbol: class X";
  2. every `Type.member` reference where `Type` is a project type resolves to a field, constant,
     nested type or method declared by that type or inherited from another *project* type
     (an unknown member inherited from a library superclass is reported as a note, never an
     error, so the SDK can never produce a false positive);
  3. every `extends` / `implements` of a project type resolves, so a deleted base class is
     caught even when nothing imports it directly;
  4. every component named in AndroidManifest.xml resolves to a real class in the tree;
  5. a short guard for SDK calls that were *removed* in newer major versions of a dependency -
     the class of build error a static checker over the app's own code cannot see otherwise.
     `AppOpenAd.load` is the one that bit us: the four-argument overload was deprecated in
     Google Mobile Ads SDK 21 and removed in a later major, so the orientation argument is
     mandatory now.

Exit code is non-zero when anything fails, so it can gate a commit - run it next to
`tools/verify_resources.py`.
"""
import glob
import os
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET

PACKAGE = 'org.Allah_Clock_Live_Wallpaper'
ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'))
if '--root' in sys.argv:
    # used by --self-test: analyse a throwaway copy of the tree
    ROOT = os.path.abspath(sys.argv[sys.argv.index('--root') + 1])
MAIN = os.path.join(ROOT, 'app', 'src', 'main')
JAVA_DIR = os.path.join(MAIN, 'java')

errors = []
notes = []


def err(msg):
    errors.append(msg)


def note(msg):
    notes.append(msg)


# ══════════════════════════ source loading and comment stripping ══════════════════════════

def strip_comments(text):
    """Blanks out comments, strings and char literals, keeping every line break in place so
    reported line numbers still match the file on disk."""
    out = []
    i, n = 0, len(text)
    state = None  # None | 'line' | 'block' | 'string' | 'char'
    while i < n:
        c = text[i]
        pair = text[i:i + 2]
        if state is None:
            if pair == '//':
                state = 'line'
                out.append('  ')
                i += 2
                continue
            if pair == '/*':
                state = 'block'
                out.append('  ')
                i += 2
                continue
            if c == '"':
                state = 'string'
            elif c == "'":
                state = 'char'
            out.append(c)
            i += 1
            continue
        if state == 'line':
            if c == '\n':
                state = None
                out.append(c)
            else:
                out.append(' ')
            i += 1
            continue
        if state == 'block':
            if pair == '*/':
                state = None
                out.append('  ')
                i += 2
                continue
            out.append('\n' if c == '\n' else ' ')
            i += 1
            continue
        # inside a string or char literal
        if c == '\\':
            out.append('  ')
            i += 2
            continue
        if (state == 'string' and c == '"') or (state == 'char' and c == "'"):
            state = None
        out.append(' ' if c != '\n' else c)
        i += 1
    return ''.join(out)


sources = {}
for path in sorted(glob.glob(os.path.join(JAVA_DIR, '**', '*.java'), recursive=True)):
    sources[path] = strip_comments(open(path, encoding='utf-8').read())

TYPE_DECL = re.compile(r'\b(?:class|interface|enum)\s+([A-Za-z_][A-Za-z0-9_]*)')
EXTENDS = re.compile(r'\b(?:class|interface|enum)\s+([A-Za-z_][A-Za-z0-9_]*)'
                     r'([^{;]*?)\{', re.S)
SUPER_CLAUSE = re.compile(r'\b(?:extends|implements)\b(.*?)(?=\bextends\b|\bimplements\b|$)',
                          re.S)
JAVA_KEYWORDS = {
    'if', 'for', 'while', 'switch', 'catch', 'return', 'new', 'else', 'do', 'try', 'synchronized',
}

# ══════════════════════════ symbol table ══════════════════════════
# types:  'Outer' / 'Outer.Inner' -> {'file', 'methods': set, 'fields': set, 'nested': set,
#                                      'supers': [names], 'super_unknown': bool}
types = {}
files_by_type = {}


def enclosing_chain(text, index):
    """Names of the classes that enclose `index`, outermost first."""
    chain = []
    depth_from_open = 0
    open_indexes = []
    for match in re.finditer(r'[{}]', text[:index]):
        if match.group() == '{':
            open_indexes.append(match.start())
        else:
            if open_indexes:
                open_indexes.pop()
    for open_index in open_indexes:
        window = text[max(0, open_index - 400):open_index]
        names = TYPE_DECL.findall(window)
        if names:
            chain.append(names[-1])
    return chain


def declare(qualified, path, supers):
    entry = types.setdefault(qualified, {
        'file': path, 'methods': set(), 'fields': set(), 'nested': set(),
        'supers': [], 'super_names': [], 'super_unknown': False,
    })
    entry['supers'] = [s for s in supers if s]
    files_by_type.setdefault(qualified, path)
    return entry


for path, text in sources.items():
    for match in TYPE_DECL.finditer(text):
        name = match.group(1)
        chain = [c for c in enclosing_chain(text, match.start()) if c]
        qualified = '.'.join(chain + [name]) if chain else name
        declare(qualified, path, [])
        if chain:
            types['.'.join(chain)]['nested'].add(name)

# supertypes (second pass: every type exists now)
for path, text in sources.items():
    for match in EXTENDS.finditer(text):
        name, header = match.group(1), match.group(2)
        chain = [c for c in enclosing_chain(text, match.start()) if c]
        qualified = '.'.join(chain + [name]) if chain else name
        parsed = []
        for clause in SUPER_CLAUSE.findall(header):
            for piece in clause.split(','):
                piece = re.sub(r'<.*>', '', piece).strip()
                if piece:
                    parsed.append(piece)
        simple_supers = [s.split('.')[0] for s in parsed]
        declare(qualified, path, simple_supers)
        types[qualified]['super_names'] = parsed
        for super_name in simple_supers:
            if super_name in types and super_name != name:
                types[qualified]['supers'].append(super_name)
            elif super_name not in ('Object',):
                types[qualified]['super_unknown'] = True


# ══════════════════════════ members ══════════════════════════
MEMBER_DECL = re.compile(
    r'(?:^|[;{}])\s*'
    r'(?:(?:public|protected|private|static|final|abstract|synchronized|native|transient|'
    r'volatile|default|strictfp|@\w+(?:\([^)]*\))?)\s+)*'
    r'[\w<>\[\],.\s?]+?\s+([A-Za-z_][A-Za-z0-9_]*)\s*'
    r'(\(|[=;,])', re.M)

for path, text in sources.items():
    for match in TYPE_DECL.finditer(text):
        name = match.group(1)
        chain = [c for c in enclosing_chain(text, match.start()) if c]
        qualified = '.'.join(chain + [name]) if chain else name
        entry = types.get(qualified)
        if entry is None:
            continue
        # body of this declaration: from its opening brace to the matching closing brace
        start = text.find('{', match.start())
        if start < 0:
            continue
        depth, end = 0, len(text)
        for position in range(start, len(text)):
            if text[position] == '{':
                depth += 1
            elif text[position] == '}':
                depth -= 1
                if depth == 0:
                    end = position
                    break
        body = text[start:end]
        for member in MEMBER_DECL.finditer(body):
            member_name, following = member.group(1), member.group(2)
            if member_name in JAVA_KEYWORDS or member_name == name:
                continue
            if following == '(':
                entry['methods'].add(member_name)
            else:
                entry['fields'].add(member_name)


def resolves(qualified, member):
    """True when `member` is declared by `qualified` or by a project supertype."""
    pending = [qualified]
    seen = set()
    while pending:
        current = pending.pop()
        if current in seen:
            continue
        seen.add(current)
        entry = types.get(current)
        if entry is None:
            continue
        if member in entry['methods'] or member in entry['fields'] or member in entry['nested']:
            return True
        pending.extend(entry['supers'])
    return False


def ancestors_have_unknown_super(qualified):
    pending = [qualified]
    seen = set()
    while pending:
        current = pending.pop()
        if current in seen:
            continue
        seen.add(current)
        entry = types.get(current)
        if entry is None:
            continue
        if entry.get('super_unknown'):
            return True
        pending.extend(entry['supers'])
    return False


# ══════════════════════════ 1. imports resolve ══════════════════════════
# The resource class R is generated by AAPT2 at build time, so it is not a .java file here.
GENERATED = (PACKAGE + '.R', PACKAGE + '.BuildConfig')
imports_checked = 0
IMPORT = re.compile(r'^\s*import\s+(?:static\s+)?(' + re.escape(PACKAGE) + r'[.\w]*)\s*;',
                    re.M)


def java_file_of(*segments):
    """The .java path a fully qualified type would live in."""
    return os.path.join(JAVA_DIR, *PACKAGE.split('.'), *segments) + '.java'


for path, text in sources.items():
    for match in IMPORT.finditer(text):
        target = match.group(1)
        imports_checked += 1
        if target == GENERATED[0] or target.startswith(GENERATED[0] + '.') \
                or target == GENERATED[1]:
            continue
        relative = target[len(PACKAGE) + 1:]
        outer_relative, _, last = relative.rpartition('.')
        # (a) a top-level class: everything before the last dot is the sub-package
        expected = java_file_of(*outer_relative.split('.'), last) if outer_relative \
            else java_file_of(last)
        if os.path.isfile(expected):
            continue
        # (b) a nested class: the last two segments are Outer.Inner
        package_part, _, outer = outer_relative.rpartition('.')
        expected_outer = java_file_of(*package_part.split('.'), outer) if package_part \
            else java_file_of(outer)
        nested = '%s.%s' % (outer, last)
        if nested in types and types[nested]['file'] == expected_outer:
            continue
        err('%s imports %s, which does not exist in the tree'
            % (os.path.relpath(path, ROOT), target))


# ══════════════════════════ 2. project member references resolve ══════════════════════════
references_checked = 0
MEMBER_REF = re.compile(r'\b([A-Z][A-Za-z0-9_]*)\s*\.\s*([A-Za-z_][A-Za-z0-9_]*)\s*([({.])?')
for path, text in sources.items():
    imported = {}
    for match in IMPORT.finditer(text):
        target = match.group(1)
        if target.startswith(PACKAGE + '.'):
            imported[target.split('.')[-1]] = target[len(PACKAGE) + 1:]
    # the file's own package: every type that lives in the same directory
    package_types = set()
    folder = os.path.dirname(path)
    for other_path, other_qualified in files_by_type.items():
        if os.path.dirname(other_path) == folder:
            package_types.add(other_qualified)
    chain = [c for c in enclosing_chain(text, len(text)) if c]
    for match in MEMBER_REF.finditer(text):
        receiver, member, following = match.group(1), match.group(2), match.group(3)
        if following == '{':
            # `new Type.Inner() { … }` - an anonymous implementation, not a member access
            continue
        if receiver in JAVA_KEYWORDS or member in JAVA_KEYWORDS:
            continue
        if receiver in ('R', 'BuildConfig'):
            # the generated resource / build-config table: verify_resources.py owns those
            continue
        if member in ('this', 'super', 'class'):
            # `Outer.this`, `Outer.super`, `Outer.class`: language forms, not members
            continue
        target = None
        if receiver in imported:
            target = imported[receiver]
        elif receiver in types:
            # a nested type of an enclosing class, or a same-package class
            if receiver in chain or receiver in package_types or '.' not in receiver:
                target = receiver
        elif chain and '.'.join([chain[0], receiver]) in types:
            target = '.'.join([chain[0], receiver])
        if target is None or target not in types:
            # a library type, a local variable or a generics parameter: not ours to check
            continue
        references_checked += 1
        if resolves(target, member):
            continue
        line = text[:match.start()].count('\n') + 1
        where = '%s:%d %s.%s' % (os.path.relpath(path, ROOT), line, receiver, member)
        if ancestors_have_unknown_super(target):
            note('%s - not declared here; it may come from a library superclass' % where)
        else:
            err('%s is not declared by %s or any of its supertypes' % (where, target))


# ══════════════════════════ 3. supertypes of project classes resolve ══════════════════════════
# A supertype is fine when it is a project type, when the file imports it from a library, or
# when it comes from java.lang. Anything else is reported: it means a base class was renamed or
# deleted while a subclass still points at it - a "cannot find symbol: class X" at build time.
JAVA_LANG = {
    'Object', 'Enum', 'Thread', 'RuntimeException', 'Exception', 'IllegalStateException',
    'IllegalArgumentException', 'Error', 'Throwable', 'Runnable', 'Comparable', 'Iterable',
}
library_imports = {}
for path, text in sources.items():
    names = set()
    for match in re.finditer(r'^\s*import\s+(?:static\s+)?([a-zA-Z][\w.]*)\s*;', text, re.M):
        target = match.group(1)
        if not target.startswith(PACKAGE + '.'):
            names.add(target.split('.')[-1])
    library_imports[path] = names

# Nested types of library classes that this app writes bare in an extends clause. They cannot
# be resolved statically (the outer class is what is imported), so they are named here.
NESTED_LIBRARY_TYPES = {
    'Engine',          # android.service.wallpaper.WallpaperService.Engine
}
for qualified, entry in sorted(types.items()):
    for super_name in entry.get('super_names', []):
        receiver = super_name.split('.')[0]
        if receiver in types or receiver in JAVA_LANG or receiver in JAVA_KEYWORDS:
            continue
        if receiver in library_imports.get(entry['file'], set()):
            continue
        if receiver in NESTED_LIBRARY_TYPES:
            continue
        note('%s extends/implements %s, which is neither a project type nor an imported library '
             'type' % (qualified, super_name))


# ══════════════════════════ 4. manifest components exist ══════════════════════════
# Only the tags that name a class are checked: android:name on <action>, <category> or
# <meta-data> is an action string or a preference key, not a type.
manifest = os.path.join(MAIN, 'AndroidManifest.xml')
CLASS_TAGS = {'application', 'activity', 'activity-alias', 'service', 'receiver', 'provider'}
manifest_names = 0
try:
    root = ET.parse(manifest).getroot()
    namespace = '{http://schemas.android.com/apk/res/android}'
    for node in root.iter():
        tag = node.tag.split('}')[-1]
        if tag not in CLASS_TAGS:
            continue
        name = node.get(namespace + 'name')
        if not name or tag == 'application':
            continue
        if name.startswith('.'):
            relative = name[1:]
        elif name.startswith(PACKAGE + '.'):
            relative = name[len(PACKAGE) + 1:]
        else:
            # a third-party component (an AdMob activity, for instance): not ours to verify
            continue
        manifest_names += 1
        expected = os.path.join(JAVA_DIR, *PACKAGE.split('.'), *relative.split('.')) + '.java'
        if not os.path.isfile(expected):
            # the manifest may also point at the resource-level default package
            err('AndroidManifest.xml names %s, which does not exist in the tree' % name)
except (OSError, ET.ParseError) as exc:
    err('could not read AndroidManifest.xml: %s' % exc)


# ══════════════════════════ 5. SDK calls that were removed in newer majors ═══════════════════
def argument_count(text, open_paren_index):
    """Number of top-level arguments of the call whose '(' sits at `open_paren_index`."""
    depth, count, seen = 0, 1, False
    for position in range(open_paren_index, len(text)):
        char = text[position]
        if char in '([{':
            depth += 1
        elif char in ')]}':
            depth -= 1
            if depth == 0:
                return count if seen else 0
        elif char == ',' and depth == 1:
            count += 1
        elif depth >= 1:
            seen = True
    return count


SDK_GUARDS = [
    ('AppOpenAd.load', 5, 5,
     'the plain four-argument overload was deprecated in Google Mobile Ads SDK 21 and removed in '
     'a later major - pass AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT (or LANDSCAPE) as the '
     'fourth argument'),
    ('InterstitialAd.load', 4, 4, 'the SDK signature is load(Context, String, AdRequest, callback)'),
    ('RewardedAd.load', 4, 4, 'the SDK signature is load(Context, String, AdRequest, callback)'),
]
guards_checked = 0
for path, text in sources.items():
    for call, minimum, maximum, hint in SDK_GUARDS:
        for match in re.finditer(re.escape(call) + r'\s*\(', text):
            open_paren = match.end() - 1
            count = argument_count(text, open_paren)
            guards_checked += 1
            if not minimum <= count <= maximum:
                line = text[:match.start()].count('\n') + 1
                err('%s:%d %s(…) is called with %d arguments; %s'
                    % (os.path.relpath(path, ROOT), line, call, count, hint))


print('java files parsed: %d (%d types, %d imports, %d project references, %d manifest '
      'components, %d SDK calls guarded)'
      % (len(sources), len(types), imports_checked, references_checked, manifest_names,
         guards_checked))
unique_notes = sorted(set(notes))
for item in unique_notes[:20]:
    print('note:', item)
if len(unique_notes) > 20:
    print('note: … %d more' % (len(unique_notes) - 20))
if errors:
    print('\n%d ERROR(S):' % len(errors))
    for item in errors:
        print(' -', item)
    sys.exit(1)
print('\nNO ERRORS')


# ══════════════════════════ self-test ══════════════════════════
SELF_TEST_PLAN = [
    ('ads/AppOpenAdController.java',
     'AdManager.isFullScreenAdActive()',
     'AdManager.isFullScreenAdActiveRenamed()',
     'renamed method'),
    ('ads/AppOpenAdController.java',
     'new AdRequest.Builder().build(), AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,',
     'new AdRequest.Builder().build(),',
     'removed SDK overload'),
]


def self_test():
    """Plants the two mistakes this script exists to catch and asserts it reports both."""
    with tempfile.TemporaryDirectory() as tmp:
        shutil.copytree(os.path.join(MAIN, 'java', PACKAGE.split('.')[0]),
                        os.path.join(tmp, 'app', 'src', 'main', 'java', *PACKAGE.split('.')[0].split('.')),
                        dirs_exist_ok=True)
        os.makedirs(os.path.join(tmp, 'app', 'src', 'main', 'java', *PACKAGE.split('.')),
                    exist_ok=True)
        shutil.copytree(os.path.join(MAIN, 'java', *PACKAGE.split('.')),
                        os.path.join(tmp, 'app', 'src', 'main', 'java', *PACKAGE.split('.')),
                        dirs_exist_ok=True)
        shutil.copy(os.path.join(MAIN, 'AndroidManifest.xml'),
                    os.path.join(tmp, 'app', 'src', 'main', 'AndroidManifest.xml'))
        planted = []
        for relative, old, new, label in SELF_TEST_PLAN:
            path = os.path.join(tmp, 'app', 'src', 'main', 'java', *PACKAGE.split('.'), relative)
            text = open(path, encoding='utf-8').read()
            if old not in text:
                print('SELF-TEST FAILED: could not plant "%s" in %s' % (label, relative))
                return 1
            open(path, 'w', encoding='utf-8').write(text.replace(old, new, 1))
            planted.append(label)
        result = subprocess.run([sys.executable, os.path.abspath(__file__), '--root', tmp],
                                capture_output=True, text=True)
        if result.returncode == 0:
            print('SELF-TEST FAILED: the planted errors were not detected')
            print(result.stdout[-2000:])
            return 1
        missing = [label for label in planted if label == 'renamed method'
                   and 'isFullScreenAdActiveRenamed' not in result.stdout]
        missing += [label for label in planted if label == 'removed SDK overload'
                    and 'AppOpenAd.load' not in result.stdout]
        if missing:
            print('SELF-TEST FAILED: not reported: %s' % ', '.join(missing))
            return 1
        print('SELF-TEST PASSED: both planted errors are reported (%s)' % ', '.join(planted))
        return 0


if '--self-test' in sys.argv:
    # The verifier checks the app; this checks the verifier: two known build-breaking edits are
    # planted in a throwaway copy of the tree and must both be reported.
    sys.exit(self_test())
