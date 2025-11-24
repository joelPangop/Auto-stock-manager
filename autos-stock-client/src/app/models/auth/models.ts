export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;         // ex: "Bearer"
  expiresIn?: number;         // en secondes
  user?: { id: number; nom: string; email: string; role: string; };
}
