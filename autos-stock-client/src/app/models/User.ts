import {Role} from "./enums/Role";

export interface User {
  id: number;
  nom: string;
  email: string;
  role: Role;
  avatarUrl?: string;
}
