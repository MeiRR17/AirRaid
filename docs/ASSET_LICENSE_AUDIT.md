# AirRaid — Asset Inventory and Redistribution Gate (v0.5.0)

**Status: inventory complete; redistribution rights NOT verified.** This inventory is source-based, not a license grant. It covers every binary in `src/main/resources/Resource/` on the `master` baseline of v0.4.0. Every asset listed below existed before the v0.5.0 implementation and is deliberately left unchanged; source assets are not automatically safe for commercial distribution.

**Files:** 23. **Total stored binary bytes:** 4,132,075. **Verified provenance records:** 0/23. All rows require rights documentation, documented replacement with original in-house assets, or removal before any public installer or distribution package is advertised as production.

| Repository asset | Bytes | Git blob SHA (content fingerprint only) | Rights status |
| --- | ---: | --- | --- |
| `Resource/Bomb/Blue.png` | 495 | `da96a5fe59d183bb4f9dcdde989125d61919d333` | **Unknown** |
| `Resource/Bomb/Orange.png` | 493 | `ae40b0f52af9a186046cc7e41de28be05ae1baf0` | **Unknown** |
| `Resource/Font/Minecraft.ttf` | 14,488 | `85c14725a3bf6d67aaf0f03292f9b763c1654f07` | **Unknown** |
| `Resource/Font/Pixel.ttf` | 14,488 | `85c14725a3bf6d67aaf0f03292f9b763c1654f07` | **Unknown** |
| `Resource/General/background.png` | 929,784 | `6121a027b46f6b41d5908252462b7498ff47329d` | **Unknown** |
| `Resource/General/FLY_.gif` | 1,653,122 | `8c4e3e1a16d803366546f697359905cb4d5ab3b8` | **Unknown** |
| `Resource/HeartsLeft/BlueTeam.png` | 304 | `56a55275f4a3489cb18ce0ecf7475ce9df932253` | **Unknown** |
| `Resource/HeartsLeft/OrangeTeam.png` | 304 | `7f9b7fd0211efbb8212cc2e39ccc9490fde96f77` | **Unknown** |
| `Resource/Player/BLUE_TEAM/option1.png` | 180,182 | `bf447c23760e49b76f1438226bda91a675049993` | **Unknown** |
| `Resource/Player/BLUE_TEAM/option2.png` | 133,266 | `a5ed5835045783c358788969ebba3178ef60c03e` | **Unknown** |
| `Resource/Player/BLUE_TEAM/option3.png` | 125,105 | `a40276659d55c90a65e96d74bad26ab2aa8d9406` | **Unknown** |
| `Resource/Player/BLUE_TEAM/option4.png` | 137,155 | `df24492affc368c2345df57554cffd17a566bfa0` | **Unknown** |
| `Resource/Player/BLUE_TEAM/option5.png` | 127,873 | `04106b93c68c5d7c1b614eb0513aeda98a3b2229` | **Unknown** |
| `Resource/Player/ORANGE_TEAM/option1.png` | 120,879 | `46b4058fb08e3e9eaf444daba4237ef596f90fc6` | **Unknown** |
| `Resource/Player/ORANGE_TEAM/option2.png` | 119,876 | `b5f2134e61b87fa776ec6cc87a69a9b5f3bb1e65` | **Unknown** |
| `Resource/Player/ORANGE_TEAM/option3.png` | 118,580 | `1c668eb389ab2143cf64cc16a65ef6ad6d2b44ca` | **Unknown** |
| `Resource/Player/ORANGE_TEAM/option4.png` | 139,761 | `48da9b4540841675aa6ca73ba6563aff34ecfe90` | **Unknown** |
| `Resource/Player/ORANGE_TEAM/option5.png` | 215,174 | `b47ca76e9cb932fe457bae79ecd6eef1cffbedaf` | **Unknown** |
| `Resource/Target/1.png` | 118 | `683906f72aef24cdc976f92401768c5d4d2c9179` | **Unknown** |
| `Resource/Target/2.png` | 130 | `72b73b39e72ac40c9aade41f90cf8d037621b102` | **Unknown** |
| `Resource/Target/3.png` | 128 | `f1c4696cd9c7e682603c6f21ad8c997c773bdb15` | **Unknown** |
| `Resource/Target/4.png` | 135 | `2f3ef3aeb8f97061320c16b716e2e6fe292bc756` | **Unknown** |
| `Resource/Window/gameIcon.jpg` | 100,235 | `c6bfbf4bc24610f6593e0e20eeddc6a497d70a76` | **Unknown** |

## Asset provenance questions and action owners

1. **Pixel.ttf and Minecraft.ttf** have the **same Git blob SHA** `85c14725a3bf6d67aaf0f03292f9b763c1654f07`; they are not two demonstrably different fonts. The naming itself is not proof of permitted use. Verify font author, source URL, exact license and redistribution terms, or replace with known-licensed original/system typography.
2. **FLY_.gif** is an animated file of 1,653,122 bytes. Verify its original creator, whether any third-party music/video or logos appear, and permission for embedded redistribution; replace if permission cannot be shown. Because it is shown on the menu, document any accessibility controls for motion later in v0.6.0.
3. **Player/BLUE_TEAM, Player/ORANGE_TEAM** contain five variants per team; request author/source and art reuse grant for each image.
4. **Bomb, Target, General/background, HeartsLeft, Window/gameIcon** require creator identification and redistribution rights. GitHub repository visibility does not imply licensing rights.
5. Any replacement should retain readable visual contrast and existing resource paths, or update `Assets` lookups and tests together.
6. Store evidence in `docs/THIRD_PARTY_NOTICES.md` in v0.7.0 only once independently substantiated. Do not assert permissive licenses without proof.
7. For **external public releases** and native installers, unresolved unknown licenses are a release-blocking P0 gate. v0.5.0 may merge internal functional code while keeping this gate open.

## New audio implementation

`SoundEffects.java` generates short, mathematical waveforms directly into 16-bit PCM arrays. No external sound sample, MP3, recording, or music is added to the repository. Audio is disabled by default and gracefully disabled if the runtime has no audio mixer. This eliminates third-party **sound asset** licensing needs, but does not resolve legacy **image/font/GIF** provenance.

## Updating this inventory

After replacing assets, recalculate each new file's SHA and byte size and retain license evidence. The content fingerprint identifies files; it does **not** prove legal ownership. Any ambiguity must be disclosed in release notes rather than silently categorized as licensed.
