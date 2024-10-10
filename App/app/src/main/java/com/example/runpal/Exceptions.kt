package com.example.runpal

/**
 * An exception thrown when the server responds with an error message.
 */
class ServerException(msg: String?): Exception(msg) {}

/**
 * Used when a repository does not contain some data.
 */
class NotFound(msg: String?): Exception(msg) {}