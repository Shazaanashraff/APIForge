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

// Seed initial data for the Node sample API
db.users.insertMany([
  { username: 'admin', email: 'admin@example.com', passwordHash: '$2a$10$placeholder',
    role: 'ADMIN', internalNotes: 'secret internal note', createdAt: new Date() },
  { username: 'alice', email: 'alice@example.com', passwordHash: '$2a$10$placeholder',
    role: 'USER', internalNotes: '', createdAt: new Date() },
]);
db.products.insertMany([
  { name: 'Widget A', description: 'A fine widget', price: 9.99, stock: 100,
    category: 'widgets', createdAt: new Date(), updatedAt: new Date() },
  { name: 'Gadget B', description: 'A useful gadget', price: 24.99, stock: 50,
    category: 'gadgets', createdAt: new Date(), updatedAt: new Date() },
]);

print('MongoDB initialized: user created, collections created, seed data inserted.');
