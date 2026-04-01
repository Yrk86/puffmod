Temporarily deprecated project.
Please see the newest issue


# PuffMod V1
Easy Minecraft Click Gui for mcp/mixin
I code this a long time ago and it might not be perfect
you will also need rounded utils for this to work

Please note that you will need animationengine for the animation to work
https://github.com/nullpointerexceptionkek/AnimationEngine



I am currently working on a new project that renders ClickGui on the user's web browser, which will be open-source after reaching a stable status.

a demo is shown below

![image](https://github.com/user-attachments/assets/5092f79e-7ded-4b0b-9826-5abdb6a0cd48)

## Forge 1.9.4 setup

- Build uses ForgeGradle 2.2 targeting Minecraft 1.9.4 (`./gradlew` uses the bundled wrapper).
- First run may take a while to download the decompiled Minecraft artifacts: `./gradlew setupDecompWorkspace`.
- Launch a test client with the GUI bound to Right Shift: `./gradlew runClient`.
