export interface LoginCommand {
  username: string
  password: string
}

export interface RefreshTokenCommand {
  refreshToken: string
}

export interface LogoutCommand {
  refreshToken?: string
}

export interface CurrentUserDTO {
  id: number
  username: string
  displayName: string
  roles: string[]
  permissions: string[]
  dataScope: string
}

export interface AuthTokenDTO {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
  currentUser: CurrentUserDTO
}
