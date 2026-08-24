#!/usr/bin/env node
/**
 * One-time seed script for the original 9 Nguon 2026 boutique demo products.
 *
 * Uploads each product's real photos (from the frontend repo's src/assets/product-*.jpg)
 * to the backend's MinIO-backed /api/files/upload/shop endpoint, then creates the 9
 * products via /api/shop-products with the resulting media references.
 *
 * This is NOT run automatically on boot (see DataInitializer, which only seeds
 * categories) — run it manually, once, against whichever backend you want populated:
 *
 *   API_BASE=http://localhost:18080/api ASSETS_DIR=/home/kamsu-perold/NGUON/src/assets node scripts/seed-shop-products.mjs
 *
 * Safe to re-run: skips creating a product if one with the same `name` already exists.
 */

import { readFile } from "node:fs/promises";
import path from "node:path";

const API_BASE = process.env.API_BASE ?? "http://localhost:18080/api";
const ASSETS_DIR = process.env.ASSETS_DIR ?? path.resolve(process.cwd(), "../NGUON/src/assets");

async function uploadImage(fileName) {
  const filePath = path.join(ASSETS_DIR, fileName);
  const buf = await readFile(filePath);
  const form = new FormData();
  form.append("file", new Blob([buf]), fileName);
  const res = await fetch(`${API_BASE}/files/upload/shop`, { method: "POST", body: form });
  if (!res.ok) throw new Error(`Upload failed for ${fileName}: ${res.status} ${await res.text()}`);
  const { fileName: objectName } = await res.json();
  return objectName;
}

async function productExists(name) {
  const res = await fetch(`${API_BASE}/shop-products`);
  if (!res.ok) throw new Error(`Failed to list products: ${res.status}`);
  const products = await res.json();
  return products.some((p) => p.name === name);
}

async function createProduct(payload) {
  const res = await fetch(`${API_BASE}/shop-products`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(`Create failed for ${payload.name}: ${res.status} ${await res.text()}`);
  return res.json();
}

// media: [{ file, alt }] — file resolved via uploadImage()
const PRODUCTS = [
  {
    category: "artisanat", name: "Masque Bamoun sculpté", tagline: "Symbole ancestral du Royaume Bamoun",
    description: "Masque traditionnel Bamoun taillé à la main dans du bois de prunus africain par les maîtres sculpteurs de Foumban. Chaque pièce est unique, incrustée de cauris et de perles colorées selon la tradition royale. Idéal comme pièce décorative ou cadeau de prestige.",
    price: 45000, unit: "pièce", inStock: true, stockQty: 12,
    seller: "Atelier Njoya Sculptures", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 677 000 100",
    tags: ["masque", "sculpture", "bois", "artisanat"], featured: true, badge: "Exclusif Nguon",
    media: [{ file: "masks.png", alt: "Masque Bamoun sculpté" }, { file: "masks2.png", alt: "Masque Bamoun vue détail" }],
  },
  {
    category: "artisanat", name: "Poterie royale de Foumban", tagline: "La terre cuite des ancêtres Bamoun",
    description: "Vase en terre cuite façonné selon les techniques ancestrales Bamoun. Ornements géométriques gravés à la main, cuit au four traditionnel. Dimensions : 30 cm de hauteur. Pièce authentique et numérotée.",
    price: 18000, comparePrice: 22000, unit: "pièce", inStock: true, stockQty: 8,
    seller: "Coopérative des Potiers du Noun", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 655 000 200",
    tags: ["poterie", "céramique", "artisanat"], featured: false, badge: "Promo",
    media: [{ file: "product-pottery.jpg", alt: "Poterie de Foumban" }, { file: "product-pottery2.jpg", alt: "Détail poterie" }],
  },
  {
    category: "artisanat", name: "Balafon miniature décoratif", tagline: "L'instrument roi des cérémonies Bamoun",
    description: "Reproduction miniature du balafon Bamoun, instrument à percussion traditionnel utilisé lors des cérémonies royales. Fabriqué en bambou de la région du Noun avec calebasses de résonance naturelles. Longueur : 40 cm.",
    price: 25000, unit: "pièce", inStock: true, stockQty: 15,
    seller: "Luthiers du Noun", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 699 000 300",
    tags: ["musique", "balafon", "artisanat", "bambou"], featured: true, badge: "Nouveau",
    media: [{ file: "product-balafon.jpg", alt: "Balafon miniature" }, { file: "culture-ceremony.jpg", alt: "Cérémonie avec balafon" }],
  },
  {
    category: "vetements", name: "Boubou Royal Bamoun", tagline: "L'élégance de la royauté Bamoun",
    description: "Boubou grand modèle en bazin riche brodé à la main, aux couleurs bleu royal et or du Royaume Bamoun. Cousu par les tailleurs de la cité impériale selon les motifs hérités du sultan. Disponible en tailles S, M, L, XL, XXL.",
    price: 85000, unit: "pièce", inStock: true, stockQty: 6,
    seller: "Maison Mbombo Couture", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 677 000 400",
    tags: ["boubou", "bazin", "broderie", "vêtement", "royal"], featured: true, badge: "Exclusif Nguon",
    media: [{ file: "product-boubou.jpg", alt: "Boubou Royal Bamoun" }, { file: "product-boubou2.jpg", alt: "Détail broderies" }],
  },
  {
    category: "vetements", name: "Pagne Ndop traditionnel", tagline: "Le tissu sacré du peuple Bamoun",
    description: "Pagne Ndop authentique, tissu indigo à motifs réservés à la cire, technique exclusive du Royaume Bamoun inscrite au patrimoine de l'UNESCO. Dimensions : 2 m × 1,10 m. Chaque pièce est tissée à la main et légèrement différente.",
    price: 35000, comparePrice: 42000, unit: "pièce", inStock: true, stockQty: 20,
    seller: "Tisserandes du Palais", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 655 000 500",
    tags: ["ndop", "pagne", "tissu", "indigo", "patrimoine"], featured: false, badge: "Promo",
    media: [{ file: "product-ndop.jpg", alt: "Pagne Ndop — détail du tissu" }],
  },
  {
    category: "gastronomie", name: "Miel sauvage du Lac Mbapit", tagline: "Récolté sur les hauteurs de Foumbot",
    description: "Miel pur de montagne récolté par les apiculteurs traditionnels des hauteurs du Mont Mbapit, à Foumbot. Saveur florale intense, non chauffé, certifié naturel. Idéal pour accompagner les plats Bamoun ou en cadeau gourmand.",
    price: 8000, unit: "pot 500g", inStock: true, stockQty: 50,
    seller: "Ruchers du Mont Mbapit", sellerLocation: "Foumbot, Région de l'Ouest", whatsapp: "+237 699 000 600",
    tags: ["miel", "naturel", "mbapit", "bio"], featured: false,
    media: [{ file: "product-honey.jpg", alt: "Miel sauvage Mbapit" }],
  },
  {
    category: "gastronomie", name: "Café arabica du Noun", tagline: "Les arômes du terroir Bamoun",
    description: "Café arabica cultivé sur les collines du Noun, torréfié artisanalement à Foumban. Notes de caramel, chocolat et agrumes. Moulu à la demande ou en grains. Coffret cadeau disponible avec 2 sachets de 250g.",
    price: 6500, unit: "sachet 250g", inStock: true, stockQty: 100,
    seller: "Caféiers du Noun", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 677 000 700",
    tags: ["café", "arabica", "terroir", "Noun"], featured: true, badge: "Nouveau",
    media: [{ file: "product-coffee.jpg", alt: "Café du Noun" }, { file: "foumban-landscape.jpg", alt: "Plantations du Noun" }],
  },
  {
    category: "livres", name: "Histoire du Nguon", tagline: "629 ans de gouvernance Bamoun",
    description: "Ouvrage de référence sur l'histoire du Nguon depuis sa création par le Roi Ncharé Yen en 1394. Texte bilingue français/anglais, richement illustré de photographies d'archives et de rituels. 320 pages, couverture rigide.",
    price: 15000, unit: "exemplaire", inStock: true, stockQty: 30,
    seller: "Éditions Palais Royal", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 655 000 800",
    tags: ["livre", "histoire", "Nguon", "culture"], featured: false,
    media: [{ file: "product-books.jpg", alt: "Histoire du Nguon" }, { file: "product-books2.jpg", alt: "Pile de livres" }],
  },
  {
    category: "decoration", name: "Plaque en bronze Bamoun", tagline: "Art métallique ancestral du Royaume",
    description: "Plaque décorative en bronze fondu représentant un guerrier Bamoun, selon les techniques de fonte à la cire perdue transmises de génération en génération depuis le Palais de Foumban. Format 25 × 35 cm. Numérotée et signée.",
    price: 65000, unit: "pièce", inStock: true, stockQty: 4,
    seller: "Fonderie Royale de Foumban", sellerLocation: "Foumban, Région de l'Ouest", whatsapp: "+237 699 000 900",
    tags: ["bronze", "sculpture", "décoration", "patrimoine"], featured: true, badge: "Exclusif Nguon",
    media: [{ file: "product-bronze.jpg", alt: "Plaque en bronze Bamoun" }, { file: "product-bronze2.jpg", alt: "Détail bronze" }],
  },
];

async function main() {
  console.log(`Seeding shop products against ${API_BASE} (assets from ${ASSETS_DIR})`);
  for (const p of PRODUCTS) {
    if (await productExists(p.name)) {
      console.log(`skip (already exists): ${p.name}`);
      continue;
    }
    const media = [];
    for (const m of p.media) {
      const objectName = await uploadImage(m.file);
      media.push({ type: "image", url: objectName, alt: m.alt, displayOrder: media.length });
      console.log(`  uploaded ${m.file} -> ${objectName}`);
    }
    const { media: _mediaSrc, ...rest } = p;
    const created = await createProduct({ ...rest, media });
    console.log(`created: ${created.name} (id=${created.id})`);
  }
  console.log("Done.");
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
