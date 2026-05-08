import express from 'express';
import mongoose from 'mongoose';
import swaggerUi from 'swagger-ui-express';
import userRoutes from './routes/users';
import productRoutes from './routes/products';

const app = express();
const PORT = process.env.PORT || 3000;
const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/apiforge_samples';

// BUG B10: no explicit body size limit set (default is 100kb but no 413 response configured)
app.use(express.json());

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: 'sample-target-api-node' });
});

app.use('/users', userRoutes);
app.use('/products', productRoutes);

// OpenAPI docs (spec served from SpringDoc equivalent)
const openApiSpec = {
  openapi: '3.0.3',
  info: { title: 'Sample Target API (Node)', version: '1.0.0',
          description: 'Buggy Node/Express API for APIForge demos' },
  paths: {
    '/health': { get: { summary: 'Health check', responses: { '200': { description: 'OK' } } } },
    '/users': {
      get: { summary: 'List users', parameters: [
        { name: 'role', in: 'query', schema: { type: 'string' } },
        { name: 'page', in: 'query', schema: { type: 'integer', default: 0 } },
        { name: 'size', in: 'query', schema: { type: 'integer', default: 20, maximum: 100 } },
      ], responses: { '200': { description: 'User list' } } },
      post: { summary: 'Create user', responses: { '201': { description: 'Created' } } },
    },
    '/users/login': {
      post: { summary: 'Login', responses: { '200': { description: 'Token' }, '401': { description: 'Unauthorized' } } },
    },
    '/users/{id}': {
      get: { summary: 'Get user', parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
             responses: { '200': { description: 'User' }, '400': { description: 'Invalid ID' }, '404': { description: 'Not found' } } },
      put: { summary: 'Update user', parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
             responses: { '200': { description: 'Updated' }, '404': { description: 'Not found' } } },
      delete: { summary: 'Delete user', parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
                responses: { '204': { description: 'Deleted' }, '404': { description: 'Not found' } } },
    },
    '/products': {
      get: { summary: 'List products',
             parameters: [
               { name: 'page', in: 'query', schema: { type: 'integer', default: 0 } },
               { name: 'size', in: 'query', schema: { type: 'integer', default: 20, maximum: 100 } },
             ],
             responses: { '200': { description: 'Paginated products with total',
               content: { 'application/json': { schema: { type: 'object',
                 required: ['items', 'total'],
                 properties: { items: { type: 'array' }, total: { type: 'integer' } } } } } } } },
      post: { summary: 'Create product', responses: { '201': { description: 'Created' }, '400': { description: 'Validation error' } } },
    },
    '/products/search': {
      get: { summary: 'Search products',
             parameters: [{ name: 'q', in: 'query', schema: { type: 'string' } }],
             responses: { '200': { description: 'Results' } } },
    },
    '/products/{id}': {
      get: { summary: 'Get product', parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
             responses: { '200': { description: 'Product' }, '400': { description: 'Invalid ID' }, '404': { description: 'Not found' } } },
      put: { summary: 'Update product', parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
             responses: { '200': { description: 'Updated' }, '404': { description: 'Not found' } } },
      delete: { summary: 'Delete product', parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'string' } }],
                responses: { '204': { description: 'Deleted' }, '404': { description: 'Not found' } } },
    },
  },
};

app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(openApiSpec));
app.get('/api-docs/json', (_req, res) => res.json(openApiSpec));

mongoose
  .connect(MONGO_URI)
  .then(() => {
    console.log('Connected to MongoDB');
    app.listen(PORT, () =>
      console.log(`Sample Node API running on port ${PORT}`),
    );
  })
  .catch((err) => {
    console.error('MongoDB connection failed:', err);
    process.exit(1);
  });

export default app;
