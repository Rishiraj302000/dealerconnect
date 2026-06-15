export type Role = 'ADMIN' | 'RELATIONSHIP_MANAGER';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  type: string;
  email: string;
  role: Role;
}

export interface RegisterResponse {
  id: number;
  name: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface CurrentUser {
  email: string;
  role: Role;
}
