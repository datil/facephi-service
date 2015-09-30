-- name: get-user
-- Finds an existing user

SELECT * FROM "USER_ACCOUNT" WHERE username = :username

-- name: save-user!
-- Creates a new user account

INSERT INTO "USER_ACCOUNT" (username, is_active, face) VALUES (:username, :is_active, :face)

-- name: save-retrained-user!
-- Retrains an existing user updating its face

UPDATE "USER_ACCOUNT" SET face = :retrained WHERE username = :username
