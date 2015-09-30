-- name: save-user!
-- Creates a new user account

INSERT INTO "USER_ACCOUNT" (username, is_active, face) VALUES (:username, :is_active, :face)

-- name: update-user-face!
-- Updates an existing user account

UPDATE "USER_ACCOUNT" SET face = :face WHERE username = :username

-- name: activate-user!
-- Sets account is_active status to 0

UPDATE "USER_ACCOUNT" SET is_active = 0 WHERE username = :username

-- name: deactivate-user!
-- Sets account is_active status to 1

UPDATE "USER_ACCOUNT" SET is_active = 1 WHERE username = :username
