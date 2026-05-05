// Placeholder entry point — full implementation in S20.
// This file exists so the project structure is valid from day one.
import express from 'express';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// Health check — APIForge smoke tests call this
app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: 'sample-target-api-node' });
});

app.listen(PORT, () => {
  console.log(`Sample Node API running on port ${PORT}`);
});

export default app;
