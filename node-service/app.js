const express = require('express');
const inventory = require('./inventory');

const app = express();
app.use(express.json());

app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

app.get('/inventory/low-stock', (req, res) => {
  res.json({ items: inventory.getLowStockItems() });
});

app.get('/inventory/:itemId', (req, res) => {
  const item = inventory.getItem(req.params.itemId);
  if (!item) {
    return res.status(404).json({ error: `Unknown item: ${req.params.itemId}` });
  }
  res.json(item);
});

app.post('/inventory/:itemId/adjust', (req, res) => {
  const { delta } = req.body;

  if (typeof delta !== 'number' || !Number.isInteger(delta)) {
    return res.status(400).json({ error: 'delta must be an integer' });
  }

  try {
    const item = inventory.adjustStock(req.params.itemId, delta);
    return res.json(item);
  } catch (err) {
    if (err instanceof inventory.UnknownItemError) {
      return res.status(404).json({ error: err.message });
    }
    if (err instanceof inventory.InsufficientStockError) {
      return res.status(409).json({ error: err.message });
    }
    throw err;
  }
});

module.exports = app;
