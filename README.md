<img alt="minestom" height="40" src="https://rawcdn.githack.com/gist/Combimagnetron/3ecb0a233233d30cf5b4acd7acf110e6/raw/3a49bf9507d785ef689cab247d4d202e4a1cb66a/minestom.svg">
<img alt="paper" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/paper_vector.svg">
<a href="https://discord.gg/PJFAGTCyyk" target="_blank">
  <img alt="discord-plural" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-plural_vector.svg">
</a>


# Sunscreen
Sunscreen is a (very) WIP GUI library, built with ultimate platform compatability in mind. It removes the limitations of chest GUIs, meaning you can place any element anywhere! Input is also ahead of chest GUIs, with inputs ranging from clicking and hovering to text input, anywhere on the screen!

> [!IMPORTANT]
> V2 code is actively being pushed to the main branch, be wary when cloning/forking.

## V2
I am currently working on a v2 of Sunscreen because there were too many fundamental bugs with the v1 platform. V2 will have many new features and a completely new codebase and api. I've listed some new features below. (Note that v2 will not work with Iris shaders on launch).
- New keybind API
- Uniform element API
- State API to automatically update elements
- Completely revamped Webflow-inspired editor
- New themes system
- Insane performance gains on client and server (dropped text displays in favor of maps and BufferedImages in favor of a custom implementation)
- Automatic resourcepack imports
- Unlimited cursor styles (text carets for example)

### Showcase from V2
Rendered at 6fps to fit in a gif, a lot smoother in-game.
![Showcase of the library](./assets/image/showcase-gif-v2.gif)

### Showcase from V1
![Showcase of the library](./assets/image/showcase-gif.gif)

## Features
- Native support for text input
- Hover and click animations
- Client sided cursor
- FOV-Locking
- Full screen menu capability
- Replace menus from any plugin!
- Ingame editor (WIP)

## Credits and Appreciation
I'd like to offer special thanks to the following people for either providing public resources or helping significantly in any way.
- `willemdev (@OmeWillem)` for helping with the map encoding system/shader and immense amounts of support.
- `_Flaster (@FlasterMC)` for contributing to the core shaders to allow for any arbitrary screen size.
- `taiyouh (@Taiyou06)` for huge performance improvements to the map encoding system.
- `retrooper (@retrooper)` for creating and providing [packetevents](https://github.com/retrooper/packetevents) and thus saving lots of compatibility worries.
- `pianoman911 & booky10 (@pianoman911, @booky10)` for the inspiration for my own BufferedImage alternative (BufferedColorSpace) through their code in [MapEngine](https://github.com/MinceraftMC/MapEngine)
