---
navigation:
    parent: rep_intro/recu_intro-index.md
    title: Recursive AE2 Pattern Provider
    icon: recursiveae2patternprovider:recursiveae2patternprovider
categories:
- recursive_patterns
item_ids:
- recursiveae2patternprovider:recursiveae2patternprovider
---

# Recursive AE2 Pattern Provider


Recursive AE2 Pattern Provider revolutionizes Applied Energistics 2 automation by automatically generating dependency patterns for complex recipes. Say goodbye to tedious manual pattern creation and hello to effortless multi-tier crafting automation!

## ✨ Key Features

### 🎯 Automatic Pattern Generation
- **Smart Dependencies**: Automatically creates patterns for missing crafting ingredients
- **Recursive Logic**: Generates patterns for ingredients of ingredients, as deep as you configure
- **Recipe Discovery**: Only creates patterns for items that have valid crafting recipes
- **Universal Support**: Works with ALL AE2 pattern types (crafting, processing, smithing, stonecutting)

### 🔧 Per-Pattern Control
The mod introduces a **recursive tag** system that gives you complete control over which patterns should auto-generate dependencies.

### 🎨 Visual Feedback
- **Enhanced Tooltips**: Clear indicators showing recursive status and usage instructions
- **Persistent Tags**: Recursive flags are preserved even when re-encoding patterns in the Pattern Encoding Terminal

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


## 💡 Example Scenario

### Traditional AE2 Setup
To autocraft Iron Pickaxes, you manually create:
1. Iron Pickaxe pattern
2. Stick pattern  
3. Iron Ingot smelting pattern (if using raw iron)
4. Wood plank pattern (if making sticks from logs)
5. Log chopping pattern (if using whole logs)
6. ... and so on, manually tracing every dependency

### With Recursive AE2 Pattern Provider
1. Create ONE recursive Iron Pickaxe pattern
2. Install in Pattern Provider
3. **All dependencies auto-generated automatically!**

The mod traces the entire crafting tree and creates patterns for every missing intermediate step, 
making complex automation setups effortless to configure.

## 🔧 Advanced Tips

### Pattern Provider Setup
- Place recursive patterns in Pattern Providers connected to Molecular Assemblers
- The mod generates dependency patterns with the same substitute settings as the parent pattern
- Use multiple Pattern Providers for better parallel processing

### Recursion Depth Control
- **Depth 1**: Only direct ingredients
- **Depth 3**: Ingredients + their ingredients + their ingredients (recommended)
- **Unlimited (-1)**: Full dependency tree (use carefully)
- **Disabled (0)**: Normal AE2 behavior

### Performance Considerations
- Higher recursion depths generate more patterns but provide complete automation
- Monitor your Pattern Provider capacity when using deep recursion
- Consider using multiple Pattern Providers for complex recipes 