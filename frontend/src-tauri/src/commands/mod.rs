use serde::Serialize;

#[derive(Serialize)]
pub struct DesktopRuntimeInfo {
    platform: String,
    app_name: String,
}

#[tauri::command]
pub fn desktop_runtime_info() -> DesktopRuntimeInfo {
    DesktopRuntimeInfo {
        platform: std::env::consts::OS.to_string(),
        app_name: "dtools".to_string(),
    }
}
