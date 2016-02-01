-- name: increment-attempts!
-- Increments login attempts counter by one.

UPDATE USER_BLOCK SET "login_attempts" = "login_attempts" + 1 where "username" = :username

-- name: get-attempts
-- Gets the login attempts of an user.

SELECT "login_attempts" from USER_BLOCK where "username" = :username

-- name: reset-attempts!
-- Resets login attempts for an user.

UPDATE user_block SET "login_attempts" = 0 WHERE "username" = :username
