---
navigation:
  parent: recu_intro/recu_intro-index.md
  icon:
  title: Advanced Tips
categories:
  - recursive_patterns
---

# Recursive AE2 Pattern Provider

## 🔧 Advanced Tips

### Terminal Workflow Efficiency
- Use the **Recursive Pattern Toggle** directly in the Pattern Encoding Terminal to batch-create recursive patterns instantly without carrying extra crafting items in your inventory.
- The toggle state in the terminal is specific to Crafting Patterns and will automatically hide when switching to Processing or Smithing modes.
- **Configurable GUI Button**: If you prefer forcing players to craft recursive patterns via crafting table recipes only, set `enableEncodingGuiToggle = false` in the configuration file.

### Recursion Depth Control
- **Depth 1**: Only generates direct ingredients (e.g., Planks for a Chest).
- **Depth 3**: Generates ingredients + their ingredients + logs (recommended for most setups).
- **Unlimited (-1)**: Solves the entire dependency tree down to raw raw materials.
- **Disabled (0)**: Restores vanilla AE2 behavior.

### Pattern Provider Setup & Performance
- Place recursive patterns in Pattern Providers connected to Molecular Assemblers.
- Auto-generated dependency patterns inherit the substitute settings (item/fluid substitution) of their parent pattern.
- In large modpacks, keep reasonable recursion depth limits to optimize network pattern indexing times.