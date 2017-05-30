# logdyn - master branch:  [![Build Status](https://travis-ci.org/logdyn/logdyn-api.svg?branch=master)](https://travis-ci.org/logdyn/logdyn-api)

# General Information
This API is designed to enable easy logging to and from a server and clients. It was developed as part of a college project (see [the repository here](https://github.com/logdyn/yeovil_microsoft_cognitive_ai) or [the hosted implementation here](https://mattihew.servehttp.com/ymca/)) and the functionality was extracted to make this API.

## Features

- Can log to a specific session or to all sessions
- Can log from server to client or vice-versa
- Log messages include severity
- Previously logged messages for a session are stored so they can be viewed in any new instance of that sesison
- Exceptions in the program are automatically logged
- Severe messages log to all sessions
- Includes example client-side implementation
- Easy to filter
- Works with existing `java.util.logging.Level` levels

## Technologies

This API uses the `javax.websocket`, `javax.servlet.http`, and `org.json` libraries, as well as the Apache commons-lang3 library.
