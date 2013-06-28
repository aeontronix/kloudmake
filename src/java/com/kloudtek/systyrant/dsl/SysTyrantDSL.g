grammar SysTyrantDSL;

script: statement* EOF;

statement: imp=importStatement | dec=newResStatement | def=defResStatement;

importStatement: IMPORT pkg=pkgName ( COL type=anyId )? SC;

newResStatement: type=resType LCB RCB;

defResStatement: DEF type=resType LCB RCB;

// Common

pkgName: anyId ( DOT anyId )*;

resType: (pkg=pkgName COL)? type=anyId;

anyId: ID | IMPORT | INCLUDE | IMPORT | DEF | SAMEHOST | AND | OR | NOT | EQS | LIKE | REGEX | IS | NULL | EMPTY | CHILDOF | TYPE | DEPENDS;

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
SC : ';';
IMPORT : 'import';
LF: '\n' -> channel(HIDDEN);
WS: [ \r\t]+ -> channel(HIDDEN);
NB: ([0-9] | '.')+;
ID: [a-zA-Z] [a-zA-Z0-9-_]*;
UQSTRING: [a-zA-Z0-9-_/$%^&*!];
QSTRING: '\'' ( ESCAPE | ~('\''|'\\') )* '\'';
ASTRING: '"' ( ESCAPE | ~('"'|'\\') )* '"';
fragment
ESCAPE : '\\' . ;
COMMENT
    :   ( '//' ~[\r\n]* '\r'? '\n'
        | '/*' .*? '*/'
        ) -> skip
    ;
