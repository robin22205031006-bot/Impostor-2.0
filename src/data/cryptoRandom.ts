function nextRandomInt(bound: number): number {
  if (bound <= 0) return 0;
  const cryptoObj = globalThis.crypto;
  if (cryptoObj && typeof cryptoObj.getRandomValues === 'function') {
    const arr = new Uint32Array(1);
    const maxUnbiased = Math.floor(0xffffffff / bound) * bound;
    let value: number;
    do {
      cryptoObj.getRandomValues(arr);
      value = arr[0];
    } while (value >= maxUnbiased && maxUnbiased > 0);
    return value % bound;
  }
  return Math.floor(Math.random() * bound);
}

export const CryptoRandom = {
  shuffled<T>(items: readonly T[]): T[] {
    if (items.length <= 1) return [...items];
    const copy = [...items];
    for (let i = copy.length - 1; i > 0; i--) {
      const j = nextRandomInt(i + 1);
      const tmp = copy[i];
      copy[i] = copy[j];
      copy[j] = tmp;
    }
    return copy;
  },

  pickOne<T>(items: readonly T[]): T | null {
    if (items.length === 0) return null;
    if (items.length === 1) return items[0];
    return items[nextRandomInt(items.length)];
  },
};
