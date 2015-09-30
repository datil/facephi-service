-- name: save-user-log!
-- Creates a new log record

INSERT INTO "USER_LOG" (username, transaction_type, transaction_result) VALUES (:username, :transaction_type, :transaction_result)
