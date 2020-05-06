CREATE TABLE IF NOT EXISTS recipes (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  uri VARCHAR,
  summary TEXT NOT NULL,
  author VARCHAR NOT NULL,
  cookingTime INT,
  calories FLOAT,
  protein FLOAT,
  fat FLOAT,
  carbohydrates FLOAT,
  sugar FLOAT
);

CREATE TABLE IF NOT EXISTS ingredient_names (
  id SERIAL PRIMARY KEY,
  ingredient VARCHAR UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS ingredients (
  recipeId SERIAL REFERENCES recipes (id),
  ingredientId SERIAL REFERENCES ingredient_names (id),
  amount FLOAT NOT NULL,
  unit VARCHAR
);