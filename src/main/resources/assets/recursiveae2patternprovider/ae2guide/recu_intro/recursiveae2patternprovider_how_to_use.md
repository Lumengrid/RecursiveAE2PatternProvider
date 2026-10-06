---
navigation:
  parent: recu_intro/recu_intro-index.md
  icon:
  title: How to Use
categories:
  - recursive_patterns
---

# Recursive AE2 Pattern Provider

## 🎮 How to Use

### Step 1: Encode a Recursive Pattern

There are **two ways** to create a recursive pattern:

#### Method A: Direct Pattern Encoding Terminal Toggle (Recommended)
1. Open your **Pattern Encoding Terminal**.
2. Select the **Crafting Pattern** tab.
3. Next to the substitute buttons, toggle the **Recursive Pattern** button **ON** (green checkmark).
4. Insert your recipe into the crafting grid and click **Encode Pattern**.
5. The encoded pattern will automatically receive the recursive flag!

> 💡 **Note**: The GUI toggle button in the Pattern Encoding Terminal can be enabled or disabled in the configuration file via `enableEncodingGuiToggle` (default: `true`).

#### Method B: Crafting Table Recipe
1. Place a standard, already encoded AE2 pattern in a Crafting Table.
2. Add the configured recipe item (default: **Iron Ingot**).
3. Craft to convert it into a recursive version (`[AE2 Pattern] + [Recipe Item] → [Recursive Pattern]`).

*(To remove recursion using a crafting table, craft the recursive pattern alone: `[Recursive Pattern] → [Normal Pattern]`)*.

### Step 2: Install in Pattern Provider
Place the recursive pattern into any **Pattern Provider** connected to a Molecular Assembler.

### Step 3: Automatic Magic! ✨
The mod automatically generates dependency patterns for missing intermediate components in real time:
- **Sticks** (if an Iron Pickaxe needs them)
- **Wood Planks** (if making sticks from whole logs)
- **Any missing crafting dependencies in the chain!**

## 🔄 Pattern Re-encoding & Persistence

When you place an existing recursive pattern back into the Pattern Encoding Terminal to edit its recipe or settings:
- **The recursive tag is preserved automatically** upon re-encoding.
- You do not need to re-craft the pattern or manually toggle the flag unless you want to disable it.