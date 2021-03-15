# PassTGenAPI:
---
- **AKKA Routes**:
 	- /requestToken: genera y/o devuelve un token.
 	- /passwordGen: diferentes opciones:
 				- Length: def 10, longitud.
 				- Chars: alfanum(def), alfanum+/_ , alfanum+/_öá.
 				- Threewords : Boolean (def false)
 					? selecciona 3 palabras de la base de datos y sustituye los chars
 					:genera una string de length y chars
 					
