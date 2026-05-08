import mongoose, { Schema, Document } from 'mongoose';

export interface IProduct extends Document {
  name: string;
  description: string;
  price: number;
  stock: number;
  category: string;
  createdAt: Date;
  updatedAt: Date;
}

const ProductSchema = new Schema<IProduct>(
  {
    name:        { type: String, required: true },
    description: { type: String, default: '' },
    price:       { type: Number, required: true, min: 0 },
    stock:       { type: Number, default: 0, min: 0 },
    category:    { type: String, default: 'general' },
  },
  { timestamps: true },
);

// BUG B6: no index on name — search is a full collection scan
export const Product = mongoose.model<IProduct>('Product', ProductSchema);
