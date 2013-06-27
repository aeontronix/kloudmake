grammar SysTyrantDSL;

script: declareres* EOF;

declareres: ;

// Lexer

DOLLAR: '$';
AND: 'and';
OR: 'or';
NOT: 'not';
REGEX: 'regex';
TYPE: 'type';
EMPTY: 'empty';
SAMEHOST: 'samehost';
CHILDOF: 'childof';
DEPENDS: 'depends';
IS: 'is';
NULL: 'null';
EQS: 'eq';
LIKE: 'like';
EQ: '=';
FEQ: '==';
TEQ: '~=';
ARWR: '->';
ARWL: '<-';
GT: '>';
GTS: 'gt';
LT: '<';
LTS: 'lt';
NEQ: '!=';
STAR: '*';
AT: '@';
LCB: '{';
RCB: '}';
LB: '(';
RB: ')';
LSB: '[';
RSB: ']';
COMMA: ',';
COL: ':';
DOT: '.';

INCLUDE : 'include';
DEF : 'def';
NEW : 'new';
SC : ';';
IMPORT : 'import';
LF: '\n' -> skip;
WS: [ \r\t]+ -> skip;
NB: ([0-9] | '.')+;
ID: [a-zA-Z] [a-zA-Z0-9-_]*;
UQSTRING: [a-zA-Z0-9-_/$%^&*!];
QSTRING: '\'' ( ESCAPE | ~('\''|'\\') )* '\'';
ASTRING: '"' ( ESCAPE | ~('"'|'\\') )* '"';
fragment
ESCAPE : '\\' . ;
