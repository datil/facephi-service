-- name: save-user-log!
-- Creates a new log record

INSERT INTO "USER_LOG" (username, identification, transaction_type, transaction_result) VALUES (:username, :identification, :transaction_type, :transaction_result)
