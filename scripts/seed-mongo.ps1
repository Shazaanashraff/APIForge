# seed-mongo.ps1 — Seeds sample data into MongoDB for the Node.js sample API
# Usage: .\scripts\seed-mongo.ps1

Write-Host "Seeding MongoDB sample data..." -ForegroundColor Cyan

# Insert sample users (some with admin roles for auth tests)
docker exec apiforge-mongodb mongosh "mongodb://apiforge:apiforge_secret@localhost:27017/apiforge_samples" --eval @'
db.users.deleteMany({});
db.users.insertMany([
  { name: "Alice Admin",   email: "alice@example.com",   role: "admin",  password: "$2b$10$placeholder" },
  { name: "Bob User",      email: "bob@example.com",     role: "user",   password: "$2b$10$placeholder" },
  { name: "Carol User",    email: "carol@example.com",   role: "user",   password: "$2b$10$placeholder" }
]);
print("Users seeded: " + db.users.countDocuments());

db.products.deleteMany({});
db.products.insertMany([
  { name: "Widget A",  price: 9.99,  category: "widgets", stock: 100 },
  { name: "Widget B",  price: 19.99, category: "widgets", stock: 50  },
  { name: "Gadget X",  price: 49.99, category: "gadgets", stock: 25  }
]);
print("Products seeded: " + db.products.countDocuments());
'@

Write-Host "Done. Verify with MongoDB Compass at mongodb://apiforge:apiforge_secret@localhost:27017/apiforge_samples" -ForegroundColor Green
