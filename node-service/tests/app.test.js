const request = require('supertest');
const app = require('../app');

describe('GET /health', () => {
  it('returns ok status', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ status: 'ok' });
  });
});

describe('GET /inventory/:itemId', () => {
  it('returns an existing item', async () => {
    const res = await request(app).get('/inventory/surgical-gauze');
    expect(res.status).toBe(200);
    expect(res.body.itemId).toBe('surgical-gauze');
  });

  it('returns 404 for an unknown item', async () => {
    const res = await request(app).get('/inventory/does-not-exist');
    expect(res.status).toBe(404);
  });
});

describe('POST /inventory/:itemId/adjust', () => {
  it('increases stock', async () => {
    const before = await request(app).get('/inventory/rabies-vaccine');
    const res = await request(app).post('/inventory/rabies-vaccine/adjust').send({ delta: 5 });
    expect(res.status).toBe(200);
    expect(res.body.quantity).toBe(before.body.quantity + 5);
  });

  it('decreases stock', async () => {
    const before = await request(app).get('/inventory/surgical-gauze');
    const res = await request(app).post('/inventory/surgical-gauze/adjust').send({ delta: -10 });
    expect(res.status).toBe(200);
    expect(res.body.quantity).toBe(before.body.quantity - 10);
  });

  it('rejects a non-integer delta', async () => {
    const res = await request(app).post('/inventory/surgical-gauze/adjust').send({ delta: 'five' });
    expect(res.status).toBe(400);
  });

  it('rejects an adjustment that would go negative', async () => {
    const res = await request(app).post('/inventory/amoxicillin-250mg/adjust').send({ delta: -100000 });
    expect(res.status).toBe(409);
  });

  it('returns 404 when adjusting an unknown item', async () => {
    const res = await request(app).post('/inventory/does-not-exist/adjust').send({ delta: 1 });
    expect(res.status).toBe(404);
  });
});

describe('GET /inventory/low-stock', () => {
  it('lists only items at or below their reorder threshold', async () => {
    const res = await request(app).get('/inventory/low-stock');
    expect(res.status).toBe(200);
    expect(Array.isArray(res.body.items)).toBe(true);
    expect(res.body.items.every((item) => item.quantity <= item.reorderThreshold)).toBe(true);
  });
});
