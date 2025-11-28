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

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: number;
    nom: string;
    email: string;
    role: string;
  };
}
