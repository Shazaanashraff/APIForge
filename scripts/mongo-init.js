// MongoDB initialization script — runs once when the container is first created.
// Creates the application user with read/write access to the samples database.

db = db.getSiblingDB('apiforge_samples');

db.createUser({
  user: 'apiforge',
  pwd: 'apiforge_secret',
  roles: [{ role: 'readWrite', db: 'apiforge_samples' }]
});

// Create collections so the schema is visible in Compass even before seeding
db.createCollection('users');
db.createCollection('products');
db.createCollection('orders');

print('MongoDB initialized: user created, collections created.');
