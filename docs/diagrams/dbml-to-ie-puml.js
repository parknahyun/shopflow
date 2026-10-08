// DBML을 읽어 PlantUML ERD(정보공학 표기, 까마귀발)를 만든다.
// 관계의 선택/필수(0..1, 1)와 컬럼의 필수(NOT NULL) 여부를 그림에 표시하려는 용도다.
// 사용: node dbml-to-ie-puml.js <입력.dbml> <출력.puml>
//
// 표기 규칙
//   컬럼 앞의 점(●)은 필수(NOT NULL), 없으면 NULL 허용.
//   부모 쪽 기호는 || (정확히 하나, 필수) 또는 |o (없거나 하나, 선택)이다.
//     외래키 컬럼이 NOT NULL이면 필수, NULL 허용이면 선택이다.
//   자식 쪽 기호는 o{ (0개 이상) 또는 o| (없거나 하나)이다.
//     외래키 컬럼에 유일 제약이 있으면 없거나 하나다.
//   "1개 이상" 같은 업무 규칙은 데이터베이스가 지키지 못하므로 그림에 쓰지 않는다.
//   실선은 외래키가 있는 관계, 점선은 컨텍스트 사이 식별자 참조(외래키 없음, ADR-0007)다.

const fs = require('fs');
const { Parser } = require('@dbml/core');

const [, , inPath, outPath] = process.argv;
const src = fs.readFileSync(inPath, 'utf8');
const schema = Parser.parse(src, 'dbmlv2').schemas[0];

const tables = new Map(schema.tables.map((t) => [t.name, t]));
const field = (table, col) => tables.get(table).fields.find((f) => f.name === col);

// 관계 목록 만들기: 외래키가 있는 관계와 note에 적힌 식별자 참조
const edges = [];
for (const r of schema.refs) {
  const child = r.endpoints.find((e) => e.relation === '*') || r.endpoints[0];
  const parent = r.endpoints.find((e) => e !== child);
  edges.push({ child: child.tableName, col: child.fieldNames[0], parent: parent.tableName, dashed: false });
}
for (const t of schema.tables) {
  for (const f of t.fields) {
    const m = f.note && f.note.match(/^식별자 참조[^]*?(\w+)\.(\w+)\s/);
    if (m) edges.push({ child: t.name, col: f.name, parent: m[1], dashed: true });
  }
}

const fkCols = new Map(); // 테이블.컬럼 -> 'FK' | '식별자'
for (const e of edges) fkCols.set(`${e.child}.${e.col}`, e.dashed ? '식별자' : 'FK');

const lines = [];
lines.push('@startuml');
lines.push("' 스키마 ERD (정보공학 표기)");
lines.push("' 이 파일은 docs/diagrams/dbml-to-ie-puml.js로 schema.dbml에서 만든다. 직접 고치지 않는다.");
lines.push('title 스키마 ERD (관계의 선택/필수 표시)');
lines.push('skinparam shadowing false');
lines.push('hide circle');
lines.push('skinparam linetype ortho');
lines.push('');

const entityBlock = (t) => {
  const out = [];
  out.push(`  entity "${t.name}" as ${t.name} {`);
  const pk = t.fields.filter((f) => f.pk);
  for (const f of pk) out.push(`    * ${f.name} : ${f.type.type_name} <<PK>>`);
  out.push('    --');
  for (const f of t.fields.filter((x) => !x.pk)) {
    const mark = f.not_null ? '* ' : '';
    const key = fkCols.get(`${t.name}.${f.name}`);
    const tag = key ? ` <<${key}>>` : '';
    out.push(`    ${mark}${f.name} : ${f.type.type_name}${tag}`);
  }
  out.push('  }');
  return out;
};

const grouped = new Set();
for (const g of schema.tableGroups) {
  lines.push(`rectangle "${g.note || g.name}" {`);
  for (const gt of g.tables) {
    const t = tables.get(gt.name);
    grouped.add(t.name);
    lines.push(...entityBlock(t));
  }
  lines.push('}');
  lines.push('');
}
for (const t of schema.tables) {
  if (!grouped.has(t.name)) lines.push(...entityBlock(t).map((l) => l.replace(/^ {2}/, '')));
}

lines.push('');
for (const e of edges) {
  const f = field(e.child, e.col);
  const parentSym = f.not_null ? '||' : '|o';
  const childSym = f.unique || f.pk ? 'o|' : 'o{';
  const line = e.dashed ? '..' : '--';
  lines.push(`${e.parent} ${parentSym}${line}${childSym} ${e.child}`);
}

lines.push('');
lines.push('legend right');
lines.push('  컬럼 앞의 점(●): 필수(NOT NULL), 없으면 NULL 허용');
lines.push('  || : 정확히 하나(필수)    |o : 없거나 하나(선택)');
lines.push('  o{ : 0개 이상            o| : 없거나 하나');
lines.push('  실선: 외래키 관계');
lines.push('  점선: 컨텍스트 사이 식별자 참조(외래키 없음)');
lines.push('  관계 기호는 데이터베이스가 지키는 규칙만 나타낸다.');
lines.push('endlegend');
lines.push('@enduml');

fs.writeFileSync(outPath, lines.join('\n') + '\n');
console.log(`엔티티 ${schema.tables.length}개, 관계 ${edges.length}개`);
