export const cropImageMap: Record<string, string> = {
  // FRUITS
  'apple': 'assets/crops/fruits/apple.webp',
  'banana': 'assets/crops/fruits/banana.webp',
  'mango': 'assets/crops/fruits/mango.webp',

  // VEGETABLES
  'tomato': 'assets/crops/vegetables/tomato.webp',
  'potato': 'assets/crops/vegetables/potato.webp',
  'carrot': 'assets/crops/vegetables/carrot.webp',

  // GRAINS
  'rice': 'assets/crops/grains/rice.webp',
  'wheat': 'assets/crops/grains/wheat.webp',
  'maize': 'assets/crops/grains/maize.webp',
  'corn': 'assets/crops/grains/maize.webp',

  // PULSES
  'green-gram': 'assets/crops/pulses/green-gram.webp',
  'greengram': 'assets/crops/pulses/green-gram.webp',
  'green gram': 'assets/crops/pulses/green-gram.webp',
  'moong': 'assets/crops/pulses/green-gram.webp',

  'chickpea': 'assets/crops/pulses/chickpea.webp',
  'chana': 'assets/crops/pulses/chickpea.webp',

  'black-gram': 'assets/crops/pulses/black-gram.webp',
  'blackgram': 'assets/crops/pulses/black-gram.webp',
  'black gram': 'assets/crops/pulses/black-gram.webp',
  'urad': 'assets/crops/pulses/black-gram.webp',

  // OILSEEDS
  'groundnut': 'assets/crops/oilseeds/groundnut.webp',
  'peanut': 'assets/crops/oilseeds/groundnut.webp',
  'ground nut': 'assets/crops/oilseeds/groundnut.webp',

  'sunflower': 'assets/crops/oilseeds/sunflower.webp',

  'sesame': 'assets/crops/oilseeds/sesame.webp',
  'til': 'assets/crops/oilseeds/sesame.webp'
};

/**
 * Normalizes crop name/category and automatically selects the centralized crop image.
 */
export function getCropImageUrl(cropName?: string, category?: string): string {
  if (!cropName && !category) {
    return 'assets/crops/default.svg';
  }

  const raw = (cropName || '').toLowerCase().trim();

  // 1. Direct key match in map
  if (cropImageMap[raw]) {
    return cropImageMap[raw];
  }

  // 2. Normalization / Substring search
  if (raw.includes('apple')) return cropImageMap['apple'];
  if (raw.includes('banana')) return cropImageMap['banana'];
  if (raw.includes('mango')) return cropImageMap['mango'];

  if (raw.includes('tomato')) return cropImageMap['tomato'];
  if (raw.includes('potato')) return cropImageMap['potato'];
  if (raw.includes('carrot')) return cropImageMap['carrot'];

  if (raw.includes('rice') || raw.includes('paddy') || raw.includes('basmati')) return cropImageMap['rice'];
  if (raw.includes('wheat')) return cropImageMap['wheat'];
  if (raw.includes('maize') || raw.includes('corn')) return cropImageMap['maize'];

  if (raw.includes('green gram') || raw.includes('green-gram') || raw.includes('greengram') || raw.includes('moong')) {
    return cropImageMap['green-gram'];
  }
  if (raw.includes('chickpea') || raw.includes('chana') || raw.includes('gram')) {
    return cropImageMap['chickpea'];
  }
  if (raw.includes('black gram') || raw.includes('black-gram') || raw.includes('blackgram') || raw.includes('urad')) {
    return cropImageMap['black-gram'];
  }

  if (raw.includes('groundnut') || raw.includes('peanut')) return cropImageMap['groundnut'];
  if (raw.includes('sunflower')) return cropImageMap['sunflower'];
  if (raw.includes('sesame') || raw.includes('til')) return cropImageMap['sesame'];

  // 3. Fallback by category
  const cat = (category || '').toUpperCase().trim();
  if (cat === 'FRUIT' || cat === 'FRUITS') return cropImageMap['apple'];
  if (cat === 'VEGETABLE' || cat === 'VEGETABLES') return cropImageMap['tomato'];
  if (cat === 'GRAIN' || cat === 'GRAINS') return cropImageMap['wheat'];
  if (cat === 'PULSES' || cat === 'PULSE') return cropImageMap['chickpea'];
  if (cat === 'OILSEEDS' || cat === 'OILSEED') return cropImageMap['sunflower'];

  return 'assets/crops/default.svg';
}
