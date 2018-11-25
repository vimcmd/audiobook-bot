# Audio books telegram listener

Telegram bot and parsing server bundle to listen books from different services.

> currently supported:
> - [audioknigi.club](https://audioknigi.club)  

# Usage

Send book url from audio book service to bot and receive chapters as list of mp3 links!

# Getting started

Minimal configuration is: 

##### Prepare server

abook-server `application.properties`:

- `app.audioknigi.club.hash.secret` to communicate with [audioknigi.club](https://audioknigi.club) rest service

##### Prepare bot 

abook-telegram-bot `application.properties`:

- `audiobook.telegram.bot.authorization-token` telegram bot token
- `audiobook.telegram.bot.username` telegram bot username
- `audiobook.server.api.url` to communicate with abook-server

### Run bundle

This project - composite gradle build.
If you want to open it in IDE, open `abook-bundle`. It already contains gradle wrapper.

Main gradle task is `runBundle`

```bash
$ cd audio-book/abook-bundle
$ ./gradlew runBundle

```
Wait for lines in log
```
: Started AudioBookServerApplication in 5.193 seconds (JVM running for 5.737)
: Started TelegramBotApplication in 4.251 seconds (JVM running for 4.812)
```

That's it! Enjoy your favorite audio books :)

##### Known problems and their solutions

- `TelegramApiRequestException: Error removing old webhook`  - probably something wrong with network, bot could not connect to api servers
- `javax.net.ssl.SSLHandshakeException` - your java version prior 8u141, update
