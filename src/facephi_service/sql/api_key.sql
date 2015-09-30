-- name: save-api-key!
-- Saves a new API key

INSERT INTO "API_KEY" (hashed_api_key, is_active) VALUES (:hashed_api_key, :is_active)

-- name: get-api-key
-- Gets a single API key

SELECT * FROM API_KEY WHERE hashed_api_key = :hashed_api_key
