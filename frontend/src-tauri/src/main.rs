mod commands;
mod platform;
mod shortcuts;
mod window;

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![commands::desktop_runtime_info])
        .run(tauri::generate_context!())
        .expect("failed to run dtools tauri app");
}
