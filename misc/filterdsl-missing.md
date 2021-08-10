# FilterDSL methoden


## Fehlende Methoden:

 * "bitLength": 0,		// bitLength()
 * ~~"as": 1,		// as(Field)~~
 * "sign": 0,		// sign()
 * "ln": 0,		// ln()
 * "cot": 0,		// cot()
 * "coth": 0,		// coth()
 * "deg": 0,		// deg()
 * "rad": 0,		// rad()
 * ~~"countDistinct": 0,		// countDistinct()~~
 * ~~"avg": 0,		// avg() ~~
 * ~~"median": 0,		// median() ~~
 * ~~" stddevPop": 0,		// stddevPop() ~~
 * ~~"stddevSamp": 0,		// stddevSamp() ~~
 * ~~"varPop": 0,		// varPop() ~~
 * ~~"varSamp": 0,		// varSamp() ~~
 * "coerce": 1,		// coerce(Field)
 * "rtrim": 0,		// rtrim()
 * "ltrim": 0,		// ltrim()
 * "rpad": 2,		// rpad(Field, Field)
 * "rpad": 1,		// rpad(Field)
 * "lpad": 2,		// lpad(Field, Field)
 * "lpad": 1,		// lpad(Field)
 * "repeat": 1,		// repeat(Field)
 * "charLength": 0,		// charLength()
 * "octetLength": 0,		// octetLength()
 * "nvl": 1,		// nvl(Field)
 * "nvl2": 2,		// nvl2(Field, Field)
 * "nullif": 1,		// nullif(Field)
 * "count": 0,		// count()
 * "abs": 0,		// abs()
 * "sin": 0,		// sin()
 * "cos": 0,		// cos()
 * "tan": 0,		// tan()
 * "atan2": 1,		// atan2(Field)
 * "sqrt": 0,		// sqrt()
 * "exp": 0,		// exp()
 * ~~"min": 0,		// min()~~
 * ~~"max": 0,		// max()~~
 * "length": 0,		// length()
 * "substring": 1,		// substring(Field)
 * "substring": 2,		// substring(Field, Field)
 * "replace": 1,		// replace(Field)
 * "replace": 2,		// replace(Field, Field)
 * "trim": 0,		// trim()
 * "decode": 2,		// decode(Field, Field)
 * "cast": 1,		// cast(Field)
 * ~~"position": 1,		// position(Field)~~
 * "sum": 0,		// sum()
 * "asin": 0,		// asin()
 * "acos": 0,		// acos()
 * "atan": 0,		// atan()
 * "ceil": 0,		// ceil()
 * "floor": 0,		// floor()
 * "round": 0,		// round()
 * "sinh": 0,		// sinh()
 * "cosh": 0,		// cosh()
 * "tanh": 0,		// tanh()
 * "ascii": 0,		// ascii()

## Fehlende Methoden (erweitert):

CONDITIONS:
 * "like": 2,		// like(String, char)
 * "like": 2,		// like(Field, char)
 * "likeIgnoreCase": 2,		// likeIgnoreCase(Field, char)
 * "likeIgnoreCase": 2,		// likeIgnoreCase(String, char)
 * "notLike": 2,		// notLike(String, char)
 * "notLike": 2,		// notLike(Field, char)
 * "notLikeIgnoreCase": 2,		// notLikeIgnoreCase(String, char)
 * "notLikeIgnoreCase": 2,		// notLikeIgnoreCase(Field, char)
 * "notIn": 1,		// notIn(Collection)
 * "notIn": 1,		// notIn(Result)
 * "notIn": 1,		// notIn(Select)
 * "compare": 2,		// compare(Comparator, Select)
 * "compare": 2,		// compare(Comparator, QuantifiedSelect)
 * "compare": 2,		// compare(Comparator, Object)
 * "compare": 2,		// compare(Comparator, Field)
 * OPERATIONS:
 * "as": 1,		// as(Function)
 * "as": 1,		// as(Name)
 * "as": 1,		// as(String)
 * "coerce": 1,		// coerce(Class)
 * "coerce": 1,		// coerce(DataType)
 * "rpad": 2,		// rpad(int, char)
 * "rpad": 1,		// rpad(int)
 * "lpad": 2,		// lpad(int, char)
 * "lpad": 1,		// lpad(int)
 * "repeat": 1,		// repeat(Number)
 * "extract": 1,		// extract(DatePart)
 * "nvl": 1,		// nvl(Object)
 * "nvl2": 2,		// nvl2(Object, Object)
 * "nullif": 1,		// nullif(Object)
 * "atan2": 1,		// atan2(Number)
 * "log": 1,		// log(int)
 * "substring": 2,		// substring(int, int)
 * "substring": 1,		// substring(int)
 * "replace": 1,		// replace(String)
 * "replace": 2,		// replace(String, String)
 * "decode": 2,		// decode(Object, Object)
 * "cast": 1,		// cast(Class)
 * "cast": 1,		// cast(DataType)
 * "position": 1,		// position(String)
 * "field": 1,		// field(Record)
 * "round": 1,		// round(int)
