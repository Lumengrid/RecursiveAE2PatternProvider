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

### Step 1: Create Your Base Pattern
Create any AE2 pattern normally using the Pattern Encoding Terminal:
- Iron Pickaxe recipe
- Complex processing pattern
- Smithing table upgrade
- Stonecutting recipe

### Step 2: Make it Recursive

**Recipe**: `[AE2 Pattern] + [Recipe Item] → [Recursive Pattern]`

1. Place your normal AE2 pattern in a crafting table
2. Add the configured recipe item (default: **Iron Ingot**)
3. Craft to create a recursive version

**Visual Indicator**: The recursive pattern will show additional tooltip information indicating its recursive status.

### Step 2b: Remove Recursion (Optional)

**Recipe**: `[Recursive Pattern] → [Normal Pattern]`

1. Place only the recursive pattern in a crafting table (no recipe item)
2. Craft to remove the recursive tag and return it to normal

### Step 3: Install in Pattern Provider
Place the recursive pattern in your Pattern Provider connected to a Molecular Assembler.

### Step 4: Automatic Magic! ✨
The mod automatically generates patterns for missing dependencies:
- **Sticks** (if Iron Pickaxe needs them)
- **Iron Ingots** (if you're using raw iron)
- **Wood Planks** (if making sticks from logs)
- **Any other missing intermediate components**

## 🔄 Pattern Re-encoding Feature

**New in Latest Version**: The recursive tag is now **preserved automatically** when you re-encode patterns!

### How It Works
1. Take a recursive pattern to the Pattern Encoding Terminal
2. Modify the recipe (change ingredients, outputs, settings, etc.)
3. Re-encode the pattern
4. **The recursive tag is automatically preserved** - no need to manually re-add it!

This means you can:
- ✅ Update recipe ingredients without losing recursion
- ✅ Change substitute settings while keeping the recursive flag
- ✅ Modify processing patterns without re-crafting the recursive version
- ✅ Update any pattern type while maintaining automation