class UnknownItemError extends Error {}
class InsufficientStockError extends Error {}

const store = {
  'amoxicillin-250mg': { itemId: 'amoxicillin-250mg', name: 'Amoxicillin 250mg', quantity: 40, reorderThreshold: 20 },
  'rabies-vaccine': { itemId: 'rabies-vaccine', name: 'Rabies Vaccine', quantity: 12, reorderThreshold: 15 },
  'surgical-gauze': { itemId: 'surgical-gauze', name: 'Surgical Gauze (box)', quantity: 75, reorderThreshold: 30 },
};

function getItem(itemId) {
  return store[itemId] || null;
}

function adjustStock(itemId, delta) {
  const item = store[itemId];
  if (!item) {
    throw new UnknownItemError(`Unknown item: ${itemId}`);
  }

  const newQuantity = item.quantity + delta;
  if (newQuantity < 0) {
    throw new InsufficientStockError(
      `Adjustment would drive "${itemId}" stock negative (current: ${item.quantity}, delta: ${delta})`
    );
  }

  item.quantity = newQuantity;
  return item;
}

function getLowStockItems() {
  return Object.values(store).filter((item) => item.quantity <= item.reorderThreshold);
}

module.exports = { getItem, adjustStock, getLowStockItems, UnknownItemError, InsufficientStockError };
