- Captura e verificação de digitais

## Setup do projeto

- Instale o JDK_17.02 ou acima
- Add a variavel local do JAVA
- Instale o Secugen WBFD driver

## Startar o projeto

mvn spring-boot:run

http://localhost:8083/escanear \
Method : Get

Cria uma nova imagem da digital. Retorna uma base64 e uma timestamp para auxiliar na verificação das digitais.

http://localhost:8083/verificar \
Method: Post\
body: fingerprint data\

Verifica a digital. Tem que passar o tempo salvo na base para verificar a digital

## Sources

https://secugen.com

## Documentação
(https://webapi.secugen.com/docs/SECUGEN_WEB_SERVICE_API_DOC.pdf)\
(https://secugen.com/drivers/)\
(https://secugen.com/guides/)
