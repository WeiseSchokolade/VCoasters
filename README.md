# VCoasters
A mod allowing mapmakers to create, design and edit rollercoasters completely within minecraft.

The mod is mostly intended to be used with coasters - it can also be used for other types of vehicles though. Some other use cases include ships, moving platforms or camera paths!

# Features
- WYSIWYG editor
- Datapack generation
- Live simulation preview
- Train speed/acceleration diagrams
- Angle sharpness guide
- Roll support
- Cart rotation independent of track
- Rotation resolving algorithm
- Precise positioning/rotation
- Position snapping
- Multiple trains with multiple carts
- Real time physics simulation
- Special track types such as lifts, brakes or stations
- Deterministic physics
- Client-side export functionality
- Development hooks for custom events along the track
- Ability to add switches, droptracks and more
- Worlds stay fully vanilla compatible

# Examples
[![Video Title](https://img.youtube.com/vi/NBazuzF6SuA/0.jpg)](https://www.youtube.com/watch?v=NBazuzF6SuA)

# Usage
Use `/editor:create my_namespace:my_track_name` to create a track and place the first piece of track. All options are shown using the ImGUI windows. You may need to adjust their position on the screen to see all of them at the same time.

To open a track, use `/editor:open my_namespace:my_track_name`. All track data is saved in vanilla command storages, so a track might not be suggested on the first time running the command.

Save a track using the provided buttons. Export using the export section. The generated datapack doesn't contain a pack.mcmeta as well as ticking functions just yet.

# Dependencies
- [Ocelot's ImGuiMC](https://modrinth.com/mod/imguimc)
