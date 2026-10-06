---
navigation:
  parent: recu_intro/recu_intro-index.md
  icon:
  title: Example Scenario
categories:
  - recursive_patterns
---

# Recursive AE2 Pattern Provider

## 💡 Example Scenario

### Traditional AE2 Setup
To autocraft an Iron Pickaxe, you traditionally must manually encode and register:
1. Iron Pickaxe pattern
2. Stick pattern
3. Wood Plank pattern (from Logs)
4. Iron Ingot smelting pattern (from Raw Iron)
5. Manually populate Pattern Providers with every sub-recipe...

### With Recursive AE2 Pattern Provider
1. Open the **Pattern Encoding Terminal**, enable the **Recursive Pattern** toggle, and encode ONE Iron Pickaxe pattern.
2. Insert it into your Pattern Provider.
3. **All required sub-patterns are generated automatically!**

The mod traces the entire crafting tree down through planks, sticks, and intermediate parts, keeping your network clean and eliminating repetitive pattern crafting.