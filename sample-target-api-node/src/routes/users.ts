import { Router, Request, Response } from 'express';
import { User } from '../models/User';

const router = Router();

// GET /users — BUG B2: NoSQL injection via ?role[$ne]=admin
router.get('/', async (req: Request, res: Response) => {
  const filter: Record<string, unknown> = {};
  if (req.query.role) {
    filter.role = req.query.role; // BUG B2: passes query object directly — allows {$ne: 'admin'}
  }
  const page = parseInt(req.query.page as string) || 0;
  const size = parseInt(req.query.size as string) || 20;
  // BUG B9 (users variant): no cap on size
  const users = await User.find(filter).skip(page * size).limit(size);
  // BUG: internalNotes leaks because we don't project it out
  res.json(users);
});

// GET /users/:id — BUG B1: invalid ObjectId → 500 (CastError unhandled)
router.get('/:id', async (req: Request, res: Response) => {
  // BUG B1: no try/catch around findById — invalid ObjectId string throws CastError → 500
  const user = await User.findById(req.params.id);
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json(user);
});

// POST /users
router.post('/', async (req: Request, res: Response) => {
  const user = new User(req.body);
  // BUG B7 (users): ValidationError not caught → unhandled → 500
  await user.save();
  res.status(201).json(user);
});

// PUT /users/:id
router.put('/:id', async (req: Request, res: Response) => {
  const user = await User.findByIdAndUpdate(req.params.id, req.body, { new: true });
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json(user);
});

// DELETE /users/:id — BUG B8: second delete returns 500
router.delete('/:id', async (req: Request, res: Response) => {
  // BUG B8: findByIdAndDelete throws DocumentNotFoundError on second call if strict mode
  const user = await User.findById(req.params.id);
  if (!user) {
    // missing: this branch is NOT hit because the line above can throw for bad IDs
    return res.status(404).json({ error: 'User not found' });
  }
  await User.deleteOne({ _id: req.params.id });
  res.status(204).send();
});

// POST /users/login — BUG B3: NoSQL injection in body
router.post('/login', async (req: Request, res: Response) => {
  const { username, password } = req.body;
  // BUG B3: passes username directly — allows { "$gt": "" } to match any user
  const user = await User.findOne({ username });
  if (!user) return res.status(401).json({ error: 'Invalid credentials' });
  const token = Buffer.from(`${user.username}:${user.role}`).toString('base64');
  res.json({ token, role: user.role });
});

export default router;
