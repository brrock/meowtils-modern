# Meowtils Documentation

Source for the [Meowtils modern](https://github.com/brrock/meowtils-modern) docs site.

The published site is [brrock.github.io/meowtils-modern](https://brrock.github.io/meowtils-modern/). GitHub Pages deploys from `.github/workflows/pages.yml` on every push to `main`.

```sh
pip install -r requirements.txt
mkdocs serve
```

Local output is `site/` (gitignored). GitHub Pages publishes that folder from `.github/workflows/pages.yml`.

Run those from this `docs/` folder. The original 1.8.9 / Forge / Lunar pages were cloned from [femboytatp/meowtils-documentation](https://github.com/femboytatp/meowtils-documentation) and rewritten for Minecraft 26.2 / Fabric.
