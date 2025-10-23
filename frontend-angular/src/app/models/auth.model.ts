export interface SignupRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: string;
  email: string;
  name: string;
}

export interface User {
  userId: string;
  email: string;
  name: string;
}
