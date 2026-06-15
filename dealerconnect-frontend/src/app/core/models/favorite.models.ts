export interface Favorite {
  id: number;
  username: string;
  dealerId: number;
  dealerName?: string;
  dealerCode?: string;
  category?: string;
  createdAt: string;
}

export interface FavoriteRequest {
  dealerId: number;
  category?: string;
}
