export type DealerStatus = 'ACTIVE' | 'INACTIVE';

export interface Dealer {
  id: number;
  name: string;
  code: string;
  email?: string;
  phone?: string;
  city?: string;
  status: DealerStatus;
  createdAt: string;
  updatedAt: string;
}

export interface DealerRequest {
  name: string;
  code: string;
  email?: string;
  phone?: string;
  city?: string;
}

export interface Contact {
  id: number;
  dealerId: number;
  name: string;
  email?: string;
  phone?: string;
  designation?: string;
  createdAt: string;
}

export interface ContactRequest {
  dealerId: number;
  name: string;
  email?: string;
  phone?: string;
  designation?: string;
}
