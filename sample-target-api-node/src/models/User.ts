import mongoose, { Schema, Document } from 'mongoose';

export interface IUser extends Document {
  username: string;
  email: string;
  passwordHash: string;
  role: 'USER' | 'ADMIN';
  internalNotes: string; // BUG B: leaks in GET /users response
  createdAt: Date;
}

const UserSchema = new Schema<IUser>({
  username: { type: String, required: true, unique: true },
  email:    { type: String, required: true, unique: true },
  passwordHash: { type: String, required: true },
  role:     { type: String, enum: ['USER', 'ADMIN'], default: 'USER' },
  internalNotes: { type: String, default: '' }, // internal field — should never appear in API responses
  createdAt: { type: Date, default: Date.now },
});

// BUG B6: no index on username for search
export const User = mongoose.model<IUser>('User', UserSchema);
