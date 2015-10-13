-- name: get-user
-- Finds an existing user

SELECT * FROM "USER_ACCOUNT" WHERE username = :username

-- name: get-user-by-identification
-- Finds an existing user by his identification

SELECT * FROM "USER_ACCOUNT" WHERE identification = :identification

-- name: save-user!
-- Creates a new user account

INSERT INTO "USER_ACCOUNT" (username, is_active, face, identification)
VALUES (:username, :is_active, :face, :identification)

-- name: save-retrained-user!
-- Retrains an existing user updating its face

UPDATE "USER_ACCOUNT" SET face = :retrained WHERE username = :username
