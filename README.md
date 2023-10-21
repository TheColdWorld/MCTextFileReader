[简体中文](./README_CN.md) | [English](./README.md)

# MCTextFileReader
A mod that can read out-of-game files in game (fabric only)

# Install
1. Download the latest version from the [Versions](https://modrinth.com/mod/textfilereader/versions) page
2. Install the mod into your mods folder
3. Start Minecraft

# Use
1. Enter the world<br>
2. Put the file(using UTF-8 encoding) you want to output in the game chat bar into [Save (in archived Texts folder) | Global (in the Texts folder of the server or client runtime directory)]<br>
3. Give file permissions to you (Requires permission level 2 or above | Enable cheating)  [use /FileReader Permission Give [FileName]]<br>
4. Type /FileReader File Read [FileName] [Save (in archived Texts folder) | Global (in the Texts folder of the server or client runtime directory)] in the chat box <br> or type /cTextFileReader Read [FileName(in the Texts folder of client runtime directory)] (client only)
5. The file content comes out

# Compilation and debugging
1. Clone this [repository](https://github.com/TheColdWorld/MCTextFileReader)<br>
2. Use your IDE to **import** build.gradle file<br>
3. Start **debugging** and **compiling**<br>
   (Optional)4. Run **gradlew build**  under the root directory of the repository to compile the mod

# License
This project is open source under the GPL-3 license