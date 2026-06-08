import type { CurrentUserDTO } from '../types/auth'

export const TOOL_EXECUTE_PERMISSION = 'tool:execute'

export function hasPermission(user: CurrentUserDTO | null, permission: string): boolean {
  return Boolean(user?.permissions.includes(permission))
}

export function hasAnyPermission(user: CurrentUserDTO | null, permissions: string[]): boolean {
  return permissions.length === 0 || permissions.some((permission) => hasPermission(user, permission))
}

export function hasAllPermissions(user: CurrentUserDTO | null, permissions: string[]): boolean {
  return permissions.every((permission) => hasPermission(user, permission))
}

export function hasDataScope(user: CurrentUserDTO | null, dataScope: string): boolean {
  return user?.dataScope === dataScope
}
