import { Router, Request, Response } from 'express';
import { Product } from '../models/Product';

const router = Router();

// GET /products — BUG B4 (off-by-one) + B5 (missing total) + B9 (no limit cap)
router.get('/', async (req: Request, res: Response) => {
  const page = parseInt(req.query.page as string) || 0;
  const size = parseInt(req.query.size as string) || 20;
  // BUG B9: no cap — ?size=100000 returns entire collection
  // BUG B4: off-by-one — returns size+1 items
  const products = await Product.find().skip(page * size).limit(size + 1);
  // BUG B5: no total count in response
  res.json({ items: products });
});

// POST /products — BUG B7: ValidationError leaks as 500
router.post('/', async (req: Request, res: Response) => {
  const product = new Product(req.body);
  // BUG B7: save() can throw Mongoose ValidationError — not caught here → 500
  const saved = await product.save();
  res.status(201).json(saved);
});

// GET /products/:id
router.get('/:id', async (req: Request, res: Response) => {
  try {
    const product = await Product.findById(req.params.id);
    if (!product) return res.status(404).json({ error: 'Product not found' });
    res.json(product);
  } catch {
    res.status(400).json({ error: 'Invalid product ID' });
  }
});

// PUT /products/:id
router.put('/:id', async (req: Request, res: Response) => {
  const product = await Product.findByIdAndUpdate(req.params.id, req.body, { new: true });
  if (!product) return res.status(404).json({ error: 'Product not found' });
  res.json(product);
});

// DELETE /products/:id — BUG B8: second delete throws 500
router.delete('/:id', async (req: Request, res: Response) => {
  // BUG B8: second delete — findById returns null → calling deleteOne on null throws
  const product = await Product.findById(req.params.id);
  await product!.deleteOne(); // BUG: no null check — second delete causes TypeError → 500
  res.status(204).send();
});

// GET /search — BUG B6: no text index, full collection scan
router.get('/search', async (req: Request, res: Response) => {
  const q = (req.query.q as string) || '';
  // BUG B6: regex search with no index — slow on large datasets
  const products = await Product.find({
    $or: [
      { name: { $regex: q, $options: 'i' } },
      { description: { $regex: q, $options: 'i' } },
    ],
  });
  res.json(products);
});

// POST /upload — BUG B10: no body size limit → no 413
router.post('/upload', (req: Request, res: Response) => {
  // BUG B10: express default body-parser has no explicit limit configured
  res.json({ uploaded: true, size: JSON.stringify(req.body).length });
});

export default router;
