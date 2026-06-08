import { invoke } from '@tauri-apps/api/core'
import type { DesktopRuntimeInfo } from '../types/tauri'

export function getDesktopRuntimeInfo(): Promise<DesktopRuntimeInfo> {
  return invoke<DesktopRuntimeInfo>('desktop_runtime_info')
}
