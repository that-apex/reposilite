/*
 * Copyright (c) 2021 dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reposilite

import com.reposilite.settings.LocalConfiguration
import com.reposilite.settings.application.SettingsWebConfiguration.LOCAL_CONFIGURATION_FILE
import com.reposilite.token.api.AccessTokenPermission.MANAGER
import com.reposilite.token.api.CreateAccessTokenRequest
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.nio.file.Path
import java.nio.file.Paths

@Command(name = "reposilite", version = ["Reposilite $VERSION"])
class ReposiliteParameters : Runnable {

    @Option(names = ["--help"], usageHelp = true, description = ["Display help message"])
    var usageHelpRequested = false

    @Option(names = ["--version", "-v"], versionHelp = true, description = ["Display current version of reposilite"])
    var versionInfoRequested = false

    @Option(names = ["--working-directory", "-wd"], description = ["Set custom working directory of application instance"])
    var workingDirectoryName = ""
    lateinit var workingDirectory: Path

    @Option(names = ["--configuration", "--config", "-cfg"], description = ["Set custom location of configuration file"])
    var configurationFileName = LOCAL_CONFIGURATION_FILE
    lateinit var configurationFile: Path

    @Option(names = ["--configuration-mode", "-cm"], description = [
        "Supported configuration modes:",
        "auto - process and override main configuration file (default)",
        "copy - load mounted configuration and save processed output in working directory",
        "print - load mounted configuration and print processed output in the console",
        "none - disable automatic updates of configuration file"
    ])
    var configurationMode = "auto"

    @Option(names = ["--hostname", "-h"], description = ["Override hostname from configuration"])
    var hostname = ""

    @Option(names = ["--port", "-p"], description = ["Override port from configuration"])
    var port = -1

    @Option(names = ["--token", "-t"], description = ["Create temporary token with the given credentials in name:secret format", "Created token has all permissions"])
    var tokenEntries = arrayOf<String>()
    lateinit var tokens: Collection<CreateAccessTokenRequest>

    @Option(names = ["--test-env", "--debug", "-d"], description = ["Enable test mode"])
    var testEnv = false

    override fun run() {
        this.workingDirectory = Paths.get(workingDirectoryName)
        this.configurationFile = workingDirectory.resolve(configurationFileName.ifEmpty { LOCAL_CONFIGURATION_FILE })
        this.tokens = tokenEntries
            .map { it.split(":", limit = 2) }
            .map { (name, secret) -> CreateAccessTokenRequest(name, secret, setOf(MANAGER)) }
    }

    fun applyLoadedConfiguration(localConfiguration: LocalConfiguration) {
        if (hostname.isEmpty()) {
            this.hostname = localConfiguration.hostname.get()
        }

        if (port == -1) {
            this.port = localConfiguration.port.get()
        }
    }

}