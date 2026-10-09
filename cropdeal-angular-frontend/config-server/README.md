# CropDeal Config Server
Runs on port 8888 and serves classpath-native configuration by default.
To use GitHub-backed configuration, set `CONFIG_SERVER_PROFILE=git` and `CONFIG_GIT_URI` to your repository.
Secrets are environment-variable driven; do not commit production credentials.
