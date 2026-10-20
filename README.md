# HP AI ASSISTANT — Android starter project

Premium-style Kotlin + Jetpack Compose starter for building from an Android phone using GitHub Actions.

## Included
- Premium Compose UI and dark-mode toggle
- Demo login/signup screens (NOT real authentication)
- Demo chat interface (NOT connected to an AI model)
- Demo admin panel (NOT secure; do not use for real admin operations)
- GitHub Actions workflow to build a debug APK
- GitHub `update.json` version check (replace the placeholder raw URL in `MainActivity.kt`)

## Build on GitHub using only your phone
1. On GitHub, create a new repository named `hp-ai-assistant`.
2. Upload the contents of this ZIP into the repository root (not the ZIP itself). Keep `.github/workflows/build-apk.yml` in that exact folder.
3. Open the repository's **Actions** tab. If prompted, enable Actions.
4. Select **Build HP AI Assistant APK** and press **Run workflow**, or wait for the first push workflow.
5. Open the completed workflow run. Under **Artifacts**, download `HP-AI-ASSISTANT-debug-apk` and extract it to get `app-debug.apk`.
6. Open the APK on your Android phone and allow installation from your browser/files app if Android asks.

## Remote update setup
1. After uploading this project, edit `update.json` in your repository.
2. Replace the placeholder download URL with your repository's Releases URL.
3. For raw JSON use this URL shape: `https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPOSITORY/main/update.json`.
4. To enforce a minimum version in a production app, wire the app's update checker to fetch that JSON and compare `minimumVersionCode` against the app's own versionCode. The Settings screen fetches the JSON and shows an update-required message when `minimumVersionCode` is higher than the installed version. Replace the placeholder raw URL in `MainActivity.kt`. This starter does not automatically install APKs or block all app access; Android requires user confirmation to install updates.
5. Publish a new APK under GitHub Releases and update the JSON version values.

## Important production notes
- Login/signup is only a UI demo. Use Firebase Authentication or your own HTTPS backend for real accounts.
- AI responses are demo text. To add a real model, call it through a backend; never embed a private AI API key in the APK.
- The admin panel is demo-only and accessible from the chat screen. Real admin functions must be enforced by a backend with verified roles.
- The APK built by this workflow is a debug APK, intended for testing. For public release, configure a signed release build and protect signing credentials in GitHub Actions secrets.
