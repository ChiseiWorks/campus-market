# -*- coding: utf-8 -*-
"""校园集市后端静态校验脚本：包名/目录一致性、括号平衡、Controller->Service 调用链核对。
本机无 JDK/Maven，用此脚本代替编译检查。检查后请删除本文件。"""
import os, re, sys, io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

BASE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(BASE, 'campus-market-server', 'src', 'main', 'java')

errors = []
files = []
for dp, _, fn in os.walk(ROOT):
    for f in fn:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

def strip_comments(s):
    # 先剥字符串/字符字面量，再剥注释，避免 URL 中的 // 被误当注释
    s = re.sub(r'"(?:\\.|[^"\\])*"', '""', s)
    s = re.sub(r"'(?:\\.|[^'\\])*'", "''", s)
    s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
    s = re.sub(r'//[^\n]*', '', s)
    return s

src_map = {}      # path -> raw source
class_map = {}    # className -> path
method_map = {}   # className -> set(method names declared)

for p in files:
    src = open(p, encoding='utf-8').read()
    src_map[p] = src
    rel = os.path.relpath(p, ROOT)
    expect = os.path.dirname(rel).replace(os.sep, '.')
    m = re.search(r'^\s*package\s+([\w.]+)\s*;', src, re.M)
    if m and m.group(1) != expect:
        errors.append('PACKAGE %s: declared %s != expected %s' % (rel, m.group(1), expect))
    code = strip_comments(src)
    if code.count('{') != code.count('}'):
        errors.append('BRACE %s: open=%d close=%d' % (rel, code.count('{'), code.count('}')))
    if code.count('(') != code.count(')'):
        errors.append('PAREN %s: open=%d close=%d' % (rel, code.count('('), code.count(')')))
    cm = re.search(r'\b(?:class|interface|enum)\s+(\w+)', code)
    if cm:
        cls = cm.group(1)
        class_map[cls] = p
        # method declarations: modifier... ReturnType name(
        methods = set(re.findall(r'\)\s*(?:throws[\w\s,]*)?\s*[{;]', code))  # placeholder
        names = set()
        for mm in re.finditer(r'(?:public|private|protected|\s)+[\w<>,.\[\]\s]+\s+(\w+)\s*\(', code):
            names.add(mm.group(1))
        method_map[cls] = names

# ---- Controller -> Service 调用链核对 ----
print('[文件总数]', len(files))
admin_ctrl_dir = os.path.join(ROOT, 'com', 'campus', 'market', 'admin', 'controller')

# 收集注入字段类型: @Resource/@Autowired 注解 或 Lombok @RequiredArgsConstructor 的 private final 字段
inject_pat = re.compile(
    r'@(?:Resource|Autowired)\s+(?:private\s+)?(?:final\s+)?(\w+)\s+(\w+)\s*;'
    r'|(?:private|protected)\s+final\s+(\w+)\s+(\w+)\s*;')

checked = 0
for p in files:
    src = src_map[p]
    code = strip_comments(src)
    rel = os.path.relpath(p, ROOT)
    for m4 in inject_pat.finditer(code):
        tm = m4.group(1) or m4.group(3)
        vm = m4.group(2) or m4.group(4)
        if tm not in class_map:
            continue
        target_src = strip_comments(src_map[class_map[tm]])
        # 找该变量上的调用 vm.method(
        for call in re.finditer(re.escape(vm) + r'\.(\w+)\s*\(', code):
            mn = call.group(1)
            if mn in ('class',):
                continue
            if mn not in method_map.get(tm, set()):
                errors.append('CALL %s: %s.%s() not found in %s' % (rel, vm, mn, tm))
            else:
                checked += 1

print('[注入调用核对] %d 处调用匹配成功' % checked)
print('[错误数]', len(errors))
for e in errors:
    print('  !!', e)
sys.exit(1 if errors else 0)
